package com.lemon.base;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import com.lemon.util.JDBCUtils;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * 所有测试用例类的父类，里面放置公用方法
 */
public class BaseCase {

    @BeforeTest
    public void globalSetup() throws FileNotFoundException {
        //整体全局性前置配置/初始化
        //1、设置项目的BaseUrl
        RestAssured.baseURI=Constants.BASE_URL;
        //2、设置接口响应结果如果是Json返回的小数类型，使用BigDecimal类型来存储
        RestAssured.config=RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        //3、设置项目的日志存储到本地文件中
        //PrintStream fileOutPutStream = new PrintStream(new File("log/test_all.log"));
        //RestAssured.filters(new RequestLoggingFilter(fileOutPutStream),new ResponseLoggingFilter(fileOutPutStream));
    }

    /**
     * 从Excel读取所需的用例数据
     *
     * @param index sheet的索引，从0开始的
     * @return caseinfo实体对象集合
     */
    public List<CaseInfo> getCaseDataFromExcel(int index) {
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File(Constants.EXCEL_PATH);
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
        return list;
    }

    /**
     * 将日志重定向到单独的文件中
     * @param interfaceName 接口模块名
     * @param caseId 用例ID
     */
    public String addLogToFile(String interfaceName,int caseId){
        String logFilePath = "";
        if(!Constants.IS_DEBUG) {
            //提前创建好目录层级 target/log/register
            String dirPath = "target/log/" + interfaceName;
            File dirFile = new File(dirPath);
            //不存在?->处理
            if (!dirFile.isDirectory()) {
                //创建目录
                dirFile.mkdirs();
            }
            logFilePath=dirPath + "/" + interfaceName + "_" + caseId + ".log";
            //请求之前对日志做配置，输出到对应文件中
            PrintStream fileOutPutStream = null;
            try {
                fileOutPutStream = new PrintStream(new File(logFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(fileOutPutStream));
        }
        return logFilePath;
    }

    /**
     * 将日志作为附件添加到Allure报告中
     * @param logFilePath 日志文件的路径
     */
    public void addLogToAllure(String logFilePath){
        if(!Constants.IS_DEBUG) {
            try {
                Allure.addAttachment("接口请求响应信息", new FileInputStream(logFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用例公共的断言方法，断言期望值和实际值
     * @param caseInfo 用例信息
     * @param res 接口的响应结果
     */
    public void assertExpected(CaseInfo caseInfo, Response res){
        //断言
        //1、把断言数据由json字符串转换为map
        ObjectMapper objectMapper2 = new ObjectMapper();
        Map expectedMap = null;
        try {
            expectedMap = objectMapper2.readValue(caseInfo.getExpected(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //2、循环遍历取到map里面每一组键值对
        Set<Map.Entry<String, Object>> set = expectedMap.entrySet();
        for (Map.Entry<String, Object> map : set){
            Object expected = map.getValue();
            if((expected instanceof Float) || (expected instanceof Double)){
                //把期望值转换(期望的json结果是小数类型-Float/Double才需要转换)
                //map.getValue() 判断一下，是不是小数类型（Float/Double）
                //把Float类型转换为BigDecimal类型
                BigDecimal bigDecimalData = new BigDecimal(map.getValue().toString());
                Assert.assertEquals(res.path(map.getKey()),bigDecimalData,"接口响应断言失败");
            }else {
                Assert.assertEquals(res.path(map.getKey()),expected,"接口响应断言失败");
            }
        }
    }

    /**
     * 数据库断言
     * @param caseInfo 用例信息
     */
    public void assertSQL(CaseInfo caseInfo){
        String checkSQL=  caseInfo.getCheckSQL();
        if(checkSQL != null) {
            Map checkSQLMap = fromJsonToMap(checkSQL);
            Set<Map.Entry<String, Object>> set = checkSQLMap.entrySet();
            for (Map.Entry<String, Object> mapEntry : set) {
                String sql = mapEntry.getKey();
                //查询数据库
                Object actual = JDBCUtils.querySingle(sql);
                System.out.println("actual::" + actual.getClass());
                System.out.println("expected::" + mapEntry.getValue().getClass());
                //1、数据库查询的返回结果是Long类型，Excel读取期望值结果是Integer
                if (actual instanceof Long) {
                    //把expected转成Long类型
                    Long expected = new Long(mapEntry.getValue().toString());
                    System.out.println("Long类型和Integer类型去断言");
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                //2、数据库查询的返回结果是BigDecimal类型，Excel读取期望值结果是Double
                }else if(actual instanceof BigDecimal){
                    BigDecimal expected = new BigDecimal(mapEntry.getValue().toString());
                    System.out.println("BigDecimal类型和Double类型去断言");
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                }else {
                    System.out.println("字符串类型断言");
                    Assert.assertEquals(actual,mapEntry.getValue(),"数据库断言失败");
                }
            }
        }
    }

    /**
     * 对所有的case参数化替换
     *
     * @param caseInfoList 当前测试类中的所有测试用例数据
     * @return 参数化替换之后的用例数据
     */
    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList) {
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
        for (CaseInfo caseInfo : caseInfoList) {
            //参数化替换请求头
            String requestHeader = regexReplace(caseInfo.getRequestHeader());
            caseInfo.setRequestHeader(requestHeader);
            //参数化替换请求地址
            String url = regexReplace(caseInfo.getUrl());
            caseInfo.setUrl(url);
            //参数化替换输入参数
            String inputParams = regexReplace(caseInfo.getInputParams());
            caseInfo.setInputParams(inputParams);
            //参数化替换期望值
            String expected = regexReplace(caseInfo.getExpected());
            caseInfo.setExpected(expected);
            //参数化替换数据库校验
            String checkSQL = regexReplace(caseInfo.getCheckSQL());
            caseInfo.setCheckSQL(checkSQL);
        }
        return caseInfoList;
    }

    /**
     * 对一条case进行参数化替换
     *
     * @param caseInfo 当前测试类中的所有测试用例数据
     * @return 参数化替换之后的用例数据
     */
    public CaseInfo paramsReplaceCaseInfo(CaseInfo caseInfo) {
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
        //参数化替换请求头
        String requestHeader = regexReplace(caseInfo.getRequestHeader());
        caseInfo.setRequestHeader(requestHeader);
        //参数化替换请求地址
        String url = regexReplace(caseInfo.getUrl());
        caseInfo.setUrl(url);
        //参数化替换输入参数
        String inputParams = regexReplace(caseInfo.getInputParams());
        caseInfo.setInputParams(inputParams);
        //参数化替换期望值
        String expected = regexReplace(caseInfo.getExpected());
        caseInfo.setExpected(expected);
        //参数化替换数据库校验
        String checkSQL = regexReplace(caseInfo.getCheckSQL());
        caseInfo.setCheckSQL(checkSQL);
        return caseInfo;

    }


    /**
     * 正则替换
     *
     * @param sourceStr 原始的字符串
     * @return 查找匹配替换之后的内容
     */
    public String regexReplace(String sourceStr) {
        //如果参数化的源字符串为null的话不需要去进行参数化替换过程
        if (sourceStr == null) {
            return sourceStr;
        }
        //  /member/{{member_id}}/info
        //1、定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2、通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3、开始进行匹配 参数：为你要去在哪一个字符串里面去进行匹配
        Matcher matcher = pattern.matcher(sourceStr);
        //保存匹配到的整个表达式，比如：{{member_id}}
        String findStr = "";
        //保存匹配到的()里面的内容  比如：member_id
        String singleStr = "";
        //4、连续查找、连续匹配
        while (matcher.find()) {
            //输出找到匹配的结果 匹配到整个正则对应的字符串内容
            findStr = matcher.group(0);
            //大括号里面的内容
            singleStr = matcher.group(1);
            //5、先去找到环境变量里面对应的值
            Object replaceStr = GlobalEnvironment.envData.get(singleStr);
            //6、替换原始字符串中的内容
            //"/member/{{member_id}}/info"  -->"/member/1111/info"
            sourceStr = sourceStr.replace(findStr, replaceStr + "");
            // "/member/{{member_id}}/info{{mobile_phone}}"
            //第一次替换之后：sourceStr --》"/member/1111/info{{mobile_phone}}"
            //第二次替换之后：sourceStr --》"/member/1111/info 13323234545"
        }

        //返回原样
        return sourceStr;
    }

    /**
     * 把json字符串转成map类型
     * @param jsonStr json字符串
     */
    public Map fromJsonToMap(String jsonStr){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
