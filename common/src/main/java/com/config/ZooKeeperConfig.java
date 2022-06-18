package com.config;


public class ZooKeeperConfig {


//    zookeeper地址
    public static String HOST = "124.221.233.199:2181";
//    zookeeper路径信息
    public static String ZK_NAMESPACE = "netty-rpc";
    public static String ZK_REGISTRY_PATH = "/registry";
    public static String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

//    连接配置项
    public static int ZK_SESSION_TIMEOUT = 5000;
    public static int ZK_CONNECTION_TIMEOUT = 5000;



}
