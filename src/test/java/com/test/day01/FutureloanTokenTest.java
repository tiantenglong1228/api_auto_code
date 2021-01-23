package com.test.day01;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class FutureloanTokenTest {

    @Test
    public void testLogin() {
        String jsonStr = "{\"mobile_phone\":\"13323231011\",\"pwd\":\"12345678\"}";
        Response res =
                given().
                        header("Content-Type", "application/json;charset=utf-8").
                        header("X-Lemonban-Media-Type", "lemonban.v2").
                        body(jsonStr).
                        when().
                        post("http://api.lemonban.com/futureloan/member/login").
                        then().
                        log().all().extract().response();
        //获取到响应信息里面所有的内容：响应头+响应体
        System.out.println(res.asString());
        //提取响应状态码
        System.out.println(res.statusCode());
        //获取接口响应时间,单位为ms
        System.out.println(res.time());
        //提取响应头
        System.out.println(res.header("Content-Type"));
        //提取响应体
        //提取响应结果对应字段值token
        //path方法-->使用Gpath路径表达式语法来提取
        String tokenValue = res.path("data.token_info.token");
        System.out.println(tokenValue);
        //提取会员id
        int memberId = res.path("data.id");
        System.out.println(memberId);

        //充值请求
        //把请求数据放到map
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("member_id", memberId);
        map.put("amount", "10000");
        given().
                header("Content-Type", "application/json;charset=utf-8").
                header("X-Lemonban-Media-Type", "lemonban.v2").
                //按照接口文档规定，如果鉴权的方式lemonban.v2，那么就必须要加token发请求
                        header("Authorization", "Bearer " + tokenValue).
                body(map).
                when().
                post("http://api.lemonban.com/futureloan/member/recharge").
                then().
                log().all();
    }

}