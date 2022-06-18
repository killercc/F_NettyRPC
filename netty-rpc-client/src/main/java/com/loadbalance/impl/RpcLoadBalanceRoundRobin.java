package com.loadbalance.impl;

import com.handler.RpcClientHandler;
import com.loadbalance.RpcLoadBalance;
import com.pojo.ProtocolInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round robin load balance
 * Created by luxiaoxun on 2020-08-01.
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public ProtocolInfo doRoute(List<ProtocolInfo> addressList) {
        int size = addressList.size();
        // Round robin
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
    }

    @Override
    public ProtocolInfo route(String serviceKey, Map<ProtocolInfo, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<ProtocolInfo>> serviceMap = getServiceMap(connectedServerNodes);
        List<ProtocolInfo> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
