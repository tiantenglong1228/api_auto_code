package com.test.day02;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CookieTest {
    Map<String,String> cookieMap = new HashMap<String, String>();
    /**
     * cookie+session鉴权
     */
    @Test
    public void testAuthenticationWithSession(){
        //登录接口请求
        Response res=
                given().
                        header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8").
                        header("X-Lemonban-Media-Type","lemonban.v2").
                        formParam("loginame","admin").formParam("password","e10adc3949ba59abbe56e057f20f883e").
                        when().
                        post("http://erp.lemfix.com/user/login").
                        then().
                        log().all().extract().response();
        cookieMap=res.getCookies();
    }

    @Test
    public void testXXX(){
        //System.out.println(res.header("Set-Cookie"));
        //推荐的
        //System.out.println("cookie::"+res.getCookies());

        //getUserSession接口请求 必须要携带cookie里面保存的sessionid
        given().
                cookies(cookieMap).
                when().
                get("http://erp.lemfix.com/user/getUserSession").
                then().
                log().all();
    }
}
