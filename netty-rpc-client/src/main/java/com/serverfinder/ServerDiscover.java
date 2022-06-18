package com.serverfinder;

import com.config.ZooKeeperConfig;
import com.manager.ConnectionManager;
import com.pojo.ProtocolInfo;
import com.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServerDiscover {

    private CuratorClient client;

    public ServerDiscover(String registyaddress) throws Exception {
        this.client = new CuratorClient(registyaddress);
        findService();
    }

    /**
     * 查找注册中心注册的服务
     * @throws Exception
     */
    public void findService() throws Exception {
        log.info("service serarching ");
        getServiceListAndUpdate();
        client.watchPathChildrenNode(ZooKeeperConfig.ZK_REGISTRY_PATH, (curatorFramework, pathChildrenCacheEvent) -> {
            final PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
            final ChildData data = pathChildrenCacheEvent.getData();
            switch (type){
                case CONNECTION_RECONNECTED:
                    log.info("zookeeper RECONNECTED and reset all service list ");
                    getServiceListAndUpdate();
                    break;
                case CHILD_ADDED:
                    log.info("zookeeper add child node ");
                    getServiceListAndUpdate(data,type);
                    break;
                case CHILD_REMOVED:
                    log.info("zookeeper remove child node ");
                    getServiceListAndUpdate(data,type);
                    break;
                case CHILD_UPDATED:
                    log.info("zookeeper update child node ");
                    getServiceListAndUpdate(data,type);
                    break;
            }
        });
    }

    private void getServiceListAndUpdate() throws Exception {
        final List<String> children = client.getChildren(ZooKeeperConfig.ZK_REGISTRY_PATH);
        List<ProtocolInfo> protocolInfos = new ArrayList<>();
        for (String child : children) {
            final byte[] data = client.getData(ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + child);
            final ProtocolInfo protocolInfo = ProtocolInfo.fromJson(new String(data));
            protocolInfos.add(protocolInfo);
        }
        updateConnectInfo(protocolInfos);
    }

    private void getServiceListAndUpdate(ChildData childData,PathChildrenCacheEvent.Type type) throws Exception {
        final ProtocolInfo protocolInfo = ProtocolInfo.fromJson(new String(childData.getData()));
        updateConnectInfo(protocolInfo,type);
    }


    private void updateConnectInfo(List<ProtocolInfo> protocolInfos){
        ConnectionManager.getInstance().updateServiceList(protocolInfos);
    }

    private void updateConnectInfo(ProtocolInfo protocolInfo,PathChildrenCacheEvent.Type type){
        ConnectionManager.getInstance().updateServiceList(protocolInfo,type);
    }

    public void stop(){
        this.client.close();
    }


}
