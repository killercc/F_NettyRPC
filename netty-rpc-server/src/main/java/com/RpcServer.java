package com;

import com.annotation.NettyRpcService;
import com.core.NettyServer;
import com.exservice.HelloService;
import com.pojo.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@Slf4j
public class RpcServer  extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {


    public RpcServer(String serverAddress, String registryAddress) {
        super(serverAddress, registryAddress);
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(NettyRpcService.class);
        for (Object serviceBean : serviceBeanMap.values()) {
            NettyRpcService nettyRpcService = serviceBean.getClass().getAnnotation(NettyRpcService.class);
            String interfaceName = nettyRpcService.value().getName();
            String version = nettyRpcService.version();
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(interfaceName);
            serviceInfo.setServicever(version);
            super.addService(serviceInfo);
        }
    }
}
