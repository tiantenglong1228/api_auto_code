package com.test.day01;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class FutureloanTest {
    @Test
    public void testRegister(){
        String jsonStr = "{\"mobile_phone\":\"13323231011\",\"pwd\":\"12345678\",\"type\":1}";
        given().
                header("Content-Type","application/json;charset=utf-8").
                header("X-Lemonban-Media-Type","lemonban.v1").
                body(jsonStr).
        when().
                post("http://api.lemonban.com/futureloan/member/register").
                then().
                log().body();
    }

    @Test
    public void testLogin(){
        String jsonStr = "{\"mobile_phone\":\"13323231011\",\"pwd\":\"12345678\"}";
        given().
                header("Content-Type","application/json;charset=utf-8").
                header("X-Lemonban-Media-Type","lemonban.v1").
                body(jsonStr).
                when().
                post("http://api.lemonban.com/futureloan/member/login").
                then().
                log().all();
    }
}
