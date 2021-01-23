package com.lemon.data;

import java.util.HashMap;
import java.util.Map;

public class GlobalEnvironment {
    //public static Integer memberId = 0;
    //public static String token;
    //优化设计--环境变量
    //换成Map形式保存环境变量
    public static Map<String,Object> envData = new HashMap<String,Object>();
}
