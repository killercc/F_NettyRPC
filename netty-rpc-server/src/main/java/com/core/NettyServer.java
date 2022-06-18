package com.core;

import com.pojo.ServiceInfo;
import com.registy.ServiceRegisty;
import com.handler.RpcServerHandlerInit;
import com.utils.ServiceKeyUtil;
import com.utils.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class NettyServer{

    private Thread thread;
    private Map<String,Object> serviceObjectMap = new HashMap<>();
    private final String serverAddress;
    private final String registryAddress;
    private final ServiceRegisty serviceRegisty;

    public NettyServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        serviceRegisty = new ServiceRegisty(registryAddress);
    }
    public void addService(ServiceInfo serviceInfo){
        String serviceKey = ServiceKeyUtil.getkey(serviceInfo.getServiceName(),serviceInfo.getServicever());
        serviceObjectMap.put(serviceKey,serviceInfo.getServiceBean());
    }

    public void start(){
        thread = new Thread(() -> {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(
                    NettyServer.class.getSimpleName(), 16, 32);
            // 主从线程模式
            EventLoopGroup bossgroup = new NioEventLoopGroup();
            EventLoopGroup workergroup = new NioEventLoopGroup();

            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossgroup,workergroup)
                    .childHandler(new RpcServerHandlerInit(serviceObjectMap,threadPoolExecutor))
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            String[] split = serverAddress.split(":");
            String host = split[0];
            int port = Integer.parseInt(split[1]);
            try {
                if(registryAddress != null){
                    serviceRegisty.registService(host,port,serviceObjectMap);
                }
                ChannelFuture channelFuture = serverBootstrap.bind(host,port).sync();
                channelFuture.channel().closeFuture().sync();
                log.info("netty server start success on port {}",port);
            } catch (InterruptedException e) {
                bossgroup.shutdownGracefully();
                workergroup.shutdownGracefully();
                log.info("netty server start failed {}",e.getMessage());
            }
        });
        thread.start();
    }
    public void destory(){
        thread = null;
        System.gc();
    }

}
