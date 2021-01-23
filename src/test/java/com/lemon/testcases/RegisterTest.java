package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import com.lemon.util.JDBCUtils;
import com.lemon.util.PhoneRandom;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class RegisterTest extends BaseCase {
    List<CaseInfo> caseInfoList;
    @BeforeClass
    public void setup(){
        //读取用例的数据
        caseInfoList =  getCaseDataFromExcel(0);
    }

    @Test(dataProvider = "getRegisterDatas")
    public void testRegister(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //随机生成三个没有注册过的手机号码
        if(caseInfo.getCaseId()==1){
            String mobilephone1 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone1",mobilephone1);
        }else if(caseInfo.getCaseId()==2){
            String mobilephone2 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone2",mobilephone2);
        }else if(caseInfo.getCaseId()==3){
            String mobilephone3 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone3",mobilephone3);
        }
        //参数化替换 --对当前的case
        caseInfo=paramsReplaceCaseInfo(caseInfo);
        Map headersMap = fromJsonToMap(caseInfo.getRequestHeader());
        String logFilePath = addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        Response res =
                given().log().all().
                        headers(headersMap).
                        body(caseInfo.getInputParams()).
                when().
                        post(caseInfo.getUrl()).
                then().log().all().
                        extract().response();
        //接口请求介绍之后把请求/响应的信息添加到allure中（附件的形式）
        //第一个参数：附件的名字 第二个参数 FileInputStream
        addLogToAllure(logFilePath);
        //断言
        //1、断言响应结果
        assertExpected(caseInfo,res);
        //2、断言数据库
        assertSQL(caseInfo);
        //在登录模块用例执行结束之后将memberId保存到环境变量中
        //注册成功的密码--从用例数据里面
        String inputParams =  caseInfo.getInputParams();
        ObjectMapper objectMapper1 = new ObjectMapper();
        Map inputParamsMap = objectMapper1.readValue(inputParams,Map.class);
        Object pwd = inputParamsMap.get("pwd");
        if(caseInfo.getCaseId()==1){
            //2、保存到环境变量中
            GlobalEnvironment.envData.put("mobile_phone1",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("member_id1",res.path("data.id"));
            GlobalEnvironment.envData.put("pwd1",pwd+"");
        }else if(caseInfo.getCaseId()==2){
            GlobalEnvironment.envData.put("mobile_phone2",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("member_id2",res.path("data.id"));
            GlobalEnvironment.envData.put("pwd2",pwd+"");
        }else if(caseInfo.getCaseId()==3){
            //2、保存到环境变量中
            GlobalEnvironment.envData.put("mobile_phone3",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("member_id3",res.path("data.id"));
            GlobalEnvironment.envData.put("pwd3",pwd+"");
        }


    }

    @DataProvider
    public Object[] getRegisterDatas(){
        //dataprovider数据提供者返回值类型可以是Object[] 也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
        //datas一维数组里面保存其实就是所有的CaseInfo对象

    }

    public static void main(String[] args) {
        String dirPath = "test/log";
        File dirFile =new File(dirPath);
        //不存在?->处理
        if (!dirFile.isDirectory()){
            //创建目录
            dirFile.mkdirs();
        }
    }
}
