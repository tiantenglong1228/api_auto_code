package com.test.day03;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.day02.CaseInfo;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class GetUserInfoTest {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(2);
        //参数化替换
        caseInfoList =  paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo) throws JsonProcessingException {
        //请求头由Json字符串转Map
        ObjectMapper objectMapper = new ObjectMapper();
        Map headersMap = objectMapper.readValue(caseInfo.getRequestHeader(), Map.class);
        Response res =
                given().
                        headers(headersMap).
                        when().
                        get("http://api.lemonban.com/futureloan"+caseInfo.getUrl()).
                        then().
                        extract().response();
        //断言

        //1、把断言数据转换为map
        ObjectMapper objectMapper2 = new ObjectMapper();
        Map expectedMap = objectMapper2.readValue(caseInfo.getExpected(), Map.class);
        //2、循环遍历取到map里面每一组键值对
        Set<Map.Entry<String, Object>> set = expectedMap.entrySet();
        for (Map.Entry<String, Object> map : set){
            //System.out.println(map.getKey());
            //System.out.println(map.getValue());
            //关键点：做断言？？？通过Gpath获取实际接口响应对应字段的值
            //我们在Excel里面写用例的期望结果时，期望结果里面键名-->Gpath表达式
            //期望结果里面键值-->期望值
            Assert.assertEquals(res.path(map.getKey()),map.getValue());
        }
    }
    @DataProvider
    public Object[] getUserInfoDatas(){
        //dataprovider数据提供者返回值类型可以是Object[] 也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
        //datas一维数组里面保存其实就是所有的CaseInfo对象

    }


    /**
     * 从Excel读取所需的用例数据
     * @param index sheet的索引，从0开始的
     */
    public List<CaseInfo> getCaseDataFromExcel(int index){
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File("src\\test\\resources\\api_testcases_futureloan_v2.xls");
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class,importParams);
        return list;
    }

    /**
     * 正则替换
     * @param sourceStr 原始的字符串
     * @return 查找匹配替换之后的内容
     */
    public String regexReplace(String sourceStr){
        //1、定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2、通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3、开始进行匹配 参数：为你要去在哪一个字符串里面去进行匹配
        Matcher matcher = pattern.matcher(sourceStr);
        //保存匹配到的整个表达式，比如：{{member_id}}
        String findStr = "";
        //保存匹配到的()里面的内容  比如：member_id
        String singleStr ="";
        //4、连续查找、连续匹配
        while(matcher.find()){
            //输出找到匹配的结果 匹配到整个正则对应的字符串内容
            findStr = matcher.group(0);
            //大括号里面的内容
            // System.out.println(matcher.group(1));
            singleStr = matcher.group(1);
        }
        //5.先去找到环境变量里面对应的值
        Object replaceStr =  GlobalEnvironment.envData.get(singleStr);
        //6、替换原始字符串中的内容
        //"/member/{{member_id}}/info"  -->"/member/1111/info"
        return sourceStr.replace(findStr,replaceStr+"");
        //return sourceStr.replace(findStr,newStr);
    }
    /**
     * 参数化替换
     * @param caseInfoList 当前测试类中的所有测试用例数据
     * @return 参数化替换之后的用例数据
     */
    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList){
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
        for (CaseInfo caseInfo : caseInfoList){
            //如果数据是为空的，没有必要去进行参数化的处理
            if(caseInfo.getRequestHeader() != null) {
                String requestHeader = regexReplace(caseInfo.getRequestHeader());
                caseInfo.setRequestHeader(requestHeader);
            }
            if(caseInfo.getUrl() != null) {
                String url = regexReplace(caseInfo.getUrl());
                caseInfo.setUrl(url);
            }
            if(caseInfo.getInputParams() != null) {
                String inputParams = regexReplace(caseInfo.getInputParams());
                caseInfo.setInputParams(inputParams);
            }
            if(caseInfo.getExpected() != null) {
                String expected = regexReplace(caseInfo.getExpected());
                caseInfo.setExpected(expected);
            }
        }
        return caseInfoList;
    }

    public static void main(String[] args) {
        Integer memberID = 1111;
        String str1 = "/member/{{member_id}}/info";
        String str2 = "{\n" +
                "    \"code\": 0,\n" +
                "    \"msg\": \"OK\",\n" +
                "    \"data.id\": {{member_id}},\n" +
                "\"data.mobile_phone\": \"13323234110\"\n" +
                "}";
        //参数化替换功能替换
        //正则表达式：
        //"." 匹配任意的字符
        //"*" 匹配前面的字符零次或者任意次数
        //"?" 贪婪匹配
        // .*?
        //1、定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2、通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3、开始进行匹配 参数：为你要去在哪一个字符串里面去进行匹配
        Matcher matcher = pattern.matcher(str1);
        //4、连续查找、连续匹配
        String findStr = "";
        while(matcher.find()){
            //输出找到匹配的结果
            findStr=matcher.group(0);
           // System.out.println(matcher.group(0));
            //System.out.println(matcher.group(1));
        }
        String outStr = str1.replace(findStr,"110110110");
        //System.out.println(outStr);


    }
}
