package com.loadbalance;

import com.handler.RpcClientHandler;
import com.pojo.ProtocolInfo;
import com.pojo.ServiceInfo;
import com.utils.ServiceKeyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RpcLoadBalance {

    protected Map<String, List<ProtocolInfo>> getServiceMap(Map<ProtocolInfo, RpcClientHandler> connectedServerNodes) {
        Map<String, List<ProtocolInfo>> serviceMap = new HashMap<>();
        if (connectedServerNodes != null && connectedServerNodes.size() > 0) {
            for (ProtocolInfo rpcProtocol : connectedServerNodes.keySet()) {
                for (ServiceInfo serviceInfo : rpcProtocol.getServiceInfoList()) {
                    String serviceKey = ServiceKeyUtil.getkey(serviceInfo.getServiceName(), serviceInfo.getServicever());
                    List<ProtocolInfo> rpcProtocolList = serviceMap.get(serviceKey);
                    if (rpcProtocolList == null) {
                        rpcProtocolList = new ArrayList<>();
                    }
                    rpcProtocolList.add(rpcProtocol);
                    serviceMap.putIfAbsent(serviceKey, rpcProtocolList);
                }
            }
        }
        return serviceMap;
    }

    // Route the connection for service key
    public abstract ProtocolInfo route(String serviceKey, Map<ProtocolInfo, RpcClientHandler> connectedServerNodes) throws Exception;
}
