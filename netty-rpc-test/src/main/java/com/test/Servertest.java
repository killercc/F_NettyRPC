package com.test;


import com.RpcServer;
import com.pojo.ServiceInfo;
import com.service.Hello;
import com.service.HelloImpl;

public class Servertest {

    public static void main(String[] args) throws Exception {

        // RPC启动地址
        String serveraddress = "127.0.0.1:8089";

        // ZooKeeper 服务器地址
        String registryaddress = "124.221.233.199:2181";


        // 连接RPC服务器
        RpcServer rpcServer = new RpcServer(serveraddress,registryaddress);
        final ServiceInfo serviceInfo = new ServiceInfo();
        Hello hello = new HelloImpl();

        // 填写服务信息
        serviceInfo.setServiceName(Hello.class.getName());
        serviceInfo.setServicever("1.0");
        serviceInfo.setServiceBean(hello);

        // 添加服务
        rpcServer.addService(serviceInfo);
        // 启动RPC服务器
        rpcServer.start();

    }
}
