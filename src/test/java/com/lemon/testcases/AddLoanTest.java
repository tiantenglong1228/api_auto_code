package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import com.test.day03.GlobalEnvironment;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

public class AddLoanTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(4);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getAddLoanDatas")
    public void testAddLoan(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json字符串转Map
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        String logFilePath = addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        //发起接口请求
        Response res =
                given().log().all().
                        headers(headersMap).
                        body(caseInfo.getInputParams()).
                when().
                        post(caseInfo.getUrl()).
                then().log().all().
                        extract().response();
        Allure.addAttachment("接口请求响应信息",new FileInputStream(logFilePath));
        //断言
        assertExpected(caseInfo,res);
        //获取项目id
        //保存到环境变量中
        if(res.path("data.id") != null) {
            GlobalEnvironment.envData.put("loan_id", res.path("data.id"));
        }
    }

    @DataProvider
    public Object[] getAddLoanDatas(){
        return caseInfoList.toArray();
    }
}
