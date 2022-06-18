package com.registy;


import com.config.ZooKeeperConfig;
import com.pojo.ProtocolInfo;
import com.utils.ServiceKeyUtil;
import lombok.extern.slf4j.Slf4j;
import com.pojo.ServiceInfo;
import com.zookeeper.CuratorClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceRegisty {

    private CuratorClient client;


    private List<String> registryList = new ArrayList<>();

    public ServiceRegisty(String connectString){
        client = new CuratorClient(connectString);
        log.info("registy server connect success");
    }

    public void registService(String host, int port, Map<String,Object> serviceObjectMap){

        List<ServiceInfo> serviceInfoList = new ArrayList<>();
        try {
            serviceObjectMap.forEach((key,value)->{
                final String[] split = key.split(ServiceKeyUtil.CONCAT_FLAG);
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setServiceName(split[0]);
                serviceInfo.setServicever(split[1]);
                serviceInfoList.add(serviceInfo);
            });
            ProtocolInfo protocolInfo = new ProtocolInfo();
            protocolInfo.setHost(host);
            protocolInfo.setPort(port);
            protocolInfo.setServiceInfoList(serviceInfoList);

            String path = ZooKeeperConfig.ZK_DATA_PATH+"-"+protocolInfo.RehashCode();
            final byte[] bytes = protocolInfo.toJson().getBytes(StandardCharsets.UTF_8);
            log.info("init node data success bytes size {}",bytes.length);
            path = this.client.createPathData(path, bytes);
            registryList.add(path);
            log.info("registy success path:{}","/"+ZooKeeperConfig.ZK_NAMESPACE+path);
        } catch (Exception e) {
            log.info("registy failed {}",e.getMessage());
        }

    }



}
