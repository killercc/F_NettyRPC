package com.utils;

public class ServiceKeyUtil {

    public static final String CONCAT_FLAG = "@";
    public static String getkey(String serviceName,String serviceVersion){
        return serviceName+CONCAT_FLAG+serviceVersion;
    }
}
