package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

public class RechargeTest extends BaseCase {

    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(3);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getRechageDatas")
    public void testRecharge(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json字符串转Map
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        String logFilePath = addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        //发起接口请求
        Response res =
                given().log().all().
                        //让REST-Assured返回json小数的时候，使用BigDecimal类型来存储小数（默认是Float存储的）
                        headers(headersMap).
                        body(caseInfo.getInputParams()).
                when().
                        post(caseInfo.getUrl()).
                then().log().all().
                        extract().response();
        Allure.addAttachment("接口请求响应信息",new FileInputStream(logFilePath));
        //接口响应断言
        assertExpected(caseInfo,res);
        //数据库断言
        assertSQL(caseInfo);
    }

    @DataProvider
    public Object[] getRechageDatas(){
        //dataprovider数据提供者返回值类型可以是Object[] 也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
        //datas一维数组里面保存其实就是所有的CaseInfo对象
    }

}
