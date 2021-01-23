package com.test.day02;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AssertTest {

    @Test
    public void testLogin(){

        String jsonStr = "{\"mobile_phone\":\"13323231011\",\"pwd\":\"12345678\"}";
        Response res =
                given().
                        header("Content-Type","application/json;charset=utf-8").
                        header("X-Lemonban-Media-Type","lemonban.v1").
                        body(jsonStr).
                        when().
                        post("http://api.lemonban.com/futureloan/member/login").
                        then().
                        log().all().extract().response();
        //获取业务码code
        int code = res.path("code");
        //获取msg
        String msg = res.path("msg");
        //获取mobile_phone
        String mobilePhone = res.path("data.mobile_phone");
        //断言--使用TestNG框架所提供的断言API
        //第一个参数：实际值 第二个参数：期望值 可以支持第三个参数（可选）：断言失败的提示信息
      Assert.assertEquals(code,0);
      Assert.assertEquals(msg,"OK");
        //如果前面断言失败，后面的不会继续运行
        //Assert.assertEquals(mobilePhone,"13323231011");
        //Assert.assertTrue(msg.equals("OK"));
        //Assert.assertFalse(msg.equals("OK"));
        //Assert.assertEquals(mobilePhone,"1332323101","断言失败");

    }
}
