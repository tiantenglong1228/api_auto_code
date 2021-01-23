package com.lemon.testcases;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class GetUserInfoTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup(){
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(2);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json字符串转Map
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        String logFilePath = addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        //发起接口请求
        Response res =
        given().log().all().
                headers(headersMap).
        when().
                get(caseInfo.getUrl()).
        then().log().all().
                extract().response();
        Allure.addAttachment("接口请求响应信息",new FileInputStream(logFilePath));
        //断言
        assertExpected(caseInfo,res);
    }

    @DataProvider
    public Object[] getUserInfoDatas(){
        //dataprovider数据提供者返回值类型可以是Object[] 也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
        //datas一维数组里面保存其实就是所有的CaseInfo对象
    }

}
