package com.test.day01;


import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class RestApiTest {

    @Test
    public void testGet01(){
        //1、简单get请求
        given().
                //given配置参数，请求头、请求参数、请求数据
                        when().
                //when是用来发起请求（get/post）
                        get("http://httpbin.org/get").
                then().
                //对响应结果做什么事情
                        log().all();
    }

    @Test
    public void testGet02(){
        //2、带参数的get请求-1
        given().
                //given配置参数，请求头、请求参数、请求数据
                        when().
                //when是用来发起请求（get/post）
                        get("http://httpbin.org/get?name=张三&age=20").
                then().
                //对响应结果做什么事情
                        log().all();
    }

    @Test
    public void testGet03() {
        //3、带参数的get请求-2
        given().
                queryParam("name","张三").queryParam("age","20").
                //given配置参数，请求头、请求参数、请求数据
                        when().
                //when是用来发起请求（get/post）
                        get("http://httpbin.org/get").
                then().
                //对响应结果做什么事情
                        log().all();
    }

    @Test
    public void testGet04() {
        //4、带参数的get请求-3 多个的情况下
        Map<String,String> map = new HashMap<String,String>();
        map.put("name","张三");
        map.put("age","20");
        map.put("address","长沙");
        map.put("sex","男");
        given().
                queryParams(map).
                //given配置参数，请求头、请求参数、请求数据
                        when().
                //when是用来发起请求（get/post）
                        get("http://httpbin.org/get").
                then().
                //对响应结果做什么事情
                        log().body();
    }

    @Test
    public void testPost01() {
        //5、发post请求-form表单参数
        //注意事项：如果form表单参数有中文的话，记得加charset=utf-8到content-type里面，否则会有乱码的问题
        given().
                formParam("name","张三").
                contentType("application/x-www-form-urlencoded;charset=utf-8").
                when().
                post("http://httpbin.org/post").
                then().
                log().body();
    }

    @Test
    public void testPost02() {
        //6、发post请求-json参数类型
        String jsonStr = "{\"mobile_phone\":\"13323231116\",\"pwd\":\"12345678\"}";
        Map<String,String> map = new HashMap<String, String>();
        map.put("mobile_phone","13323231116");
        map.put("pwd","12345678");
        given().
                contentType("application/json;charset=utf-8").
                body(map).
                when().
                post("http://httpbin.org/post").
                then().
                log().body();
    }

    @Test
    public void testPost03() {
        //7、发post请求-xml参数类型
        String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<suite>\n" +
                "    <class>测试xml</class>\n" +
                "</suite>";
        given().
                contentType("text/xml;charset=utf-8").
                body(xmlStr).
                when().
                post("http://httpbin.org/post").
                then().
                log().body();
    }

    @Test
    public void testPost04() {
        //8、发post请求-多参数表单 上传文件
        given().
                contentType("multipart/form-data;charset=utf-8").
                multiPart(new File("C:\\Users\\Administrator\\Desktop\\api_testcases_futureloan_v1.xls")).
         when().
                post("http://httpbin.org/post").
         then().
                log().body();
    }
}
