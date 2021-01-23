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

import static io.restassured.RestAssured.given;

public class LoginTest {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setUp(){
        caseInfoList =  getCaseDataFromExcel(1);
    }


    @Test(dataProvider = "getLoginDatas")
    public void testLogin01(CaseInfo caseInfo) throws JsonProcessingException {
        //String jsonStr = "{\"mobile_phone\":\"13323231011\",\"pwd\":\"12345678\"}";
        //字符串请求头转换成Map -- 下节课讲
        ObjectMapper objectMapper = new ObjectMapper();
        Map headersMap = objectMapper.readValue(caseInfo.getRequestHeader(), Map.class);
        Response res =
        given().
                //header("Content-Type",header1).
                //header("X-Lemonban-Media-Type",header2)
                headers(headersMap).
                body(caseInfo.getInputParams()).
                when().
                post("http://api.lemonban.com/futureloan"+caseInfo.getUrl()).
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
        //在登录模块用例执行结束之后将memberId保存到环境变量中
        //1、拿到正常用例返回响应信息里面的memberId
        Integer memberId = res.path("data.id");
        if(memberId != null){
            //2、保存到环境变量中
            //GlobalEnvironment.memberId=memberId;
            GlobalEnvironment.envData.put("member_id",memberId);
            //3.拿到正常用例返回信息里面的token
            String token = res.path("data.token_info.token");
            GlobalEnvironment.envData.put("token",token);
        }



    }
    @DataProvider
    public Object[] getLoginDatas(){
        //dataprovider数据提供者返回值类型可以是Object[] 也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
    }
    /**
     * 从Excel读取所需的用例数据
     * @param index sheet的索引，从0开始的
     */
    public List<CaseInfo> getCaseDataFromExcel(int index){
        //从Excel读取所有的用例数据
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File("src\\test\\resources\\api_testcases_futureloan_v2.xls");
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class,importParams);
        return list;
    }




   /* public static void main(String[] args) {
        //读取Excel测试用例数据？？poi麻烦/笨重
        //推荐读取Excel技术：EasyPOI
        //第一个参数：File对象 第二个参数：映射的实体类.class  第三个参数：读取配置对象
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(0);
        importParams.setSheetNum(2);
        File excelFile = new File("src\\test\\resources\\api_testcases_futureloan_v1.xls");
       List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class,importParams);
       for (CaseInfo caseInfo:list){
           System.out.println(caseInfo);
       }
    }*/
}
