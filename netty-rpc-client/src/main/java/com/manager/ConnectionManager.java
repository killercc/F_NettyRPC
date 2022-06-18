package com.manager;

import com.handler.RpcClientHandler;
import com.handler.RpcClientHandlerInit;
import com.loadbalance.RpcLoadBalance;
import com.loadbalance.impl.RpcLoadBalanceRoundRobin;
import com.pojo.ProtocolInfo;
import com.utils.ThreadPoolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
public class ConnectionManager {

    private static final ConnectionManager connectionManager;
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static final ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(ConnectionManager.class.getName(),
            16,32);

    private final Map<ProtocolInfo, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<ProtocolInfo> rpcProtocolSet = new CopyOnWriteArraySet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition connected = lock.newCondition();
    private final long waitTimeout = 5000;
    private final RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();
    private boolean isRunning = true;
    static {
        connectionManager = new ConnectionManager();
    }
    public static ConnectionManager getInstance(){
        return connectionManager;
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0){
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();

            } catch (Exception e){
                throw new RuntimeException("LOCK interrupt can not get handler");
            }
        }
        final ProtocolInfo protocolInfo = loadBalance.route(serviceKey, connectedServerNodes);
        return connectedServerNodes.get(protocolInfo);

    }

    public void updateServiceList(List<ProtocolInfo> protocolInfoList){
        if(protocolInfoList.isEmpty()){
            log.warn("no service set ");
        }else{
            for (ProtocolInfo protocolInfo : protocolInfoList) {
                if(!rpcProtocolSet.contains(protocolInfo)){
                    connectNode(protocolInfo);
                }
            }
        }

    }

    public void updateServiceList(ProtocolInfo protocolInfo, PathChildrenCacheEvent.Type type){

        switch (type){
            case CHILD_ADDED:
                if(rpcProtocolSet.contains(protocolInfo)){
                    connectNode(protocolInfo);
                    break;
                }else break;
            case CHILD_UPDATED:
                for (ProtocolInfo info : rpcProtocolSet) {
                    if(info != protocolInfo)connectNode(protocolInfo);
                }
                break;
            case CHILD_REMOVED:
                removeServiceInfo(protocolInfo);
                break;
            default:
                throw new IllegalArgumentException("error Type");

        }

    }
    private void connectNode(ProtocolInfo protocolInfo){
        if(null == protocolInfo.getServiceInfoList() || protocolInfo.getServiceInfoList().isEmpty()){
            log.info("service list is empty");
            return;
        }
        rpcProtocolSet.add(protocolInfo);

        threadPoolExecutor.submit(() -> {
            try {
                new Bootstrap().group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientHandlerInit())
                        .connect(protocolInfo.getHost(),protocolInfo.getPort())
                        .sync().addListener((ChannelFutureListener) channelFuture -> {
                            if(channelFuture.isSuccess()){
                                log.info("connect server success remote address:{}",protocolInfo.getHost());
                                final RpcClientHandler rpcClientHandler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                                connectedServerNodes.put(protocolInfo,rpcClientHandler);
                                rpcClientHandler.setProtocolInfo(protocolInfo);
                                signalAvailableHandler();
                            }else {
                                log.error("connect server failed ");
                            }

                        });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    private void removeServiceInfo(ProtocolInfo protocolInfo){
        RpcClientHandler rpcClientHandler = connectedServerNodes.get(protocolInfo);
        if(null != rpcClientHandler) rpcClientHandler.close();

        connectedServerNodes.remove(protocolInfo);
        rpcProtocolSet.remove(protocolInfo);

    }
    public void removeProtocol(ProtocolInfo protocolInfo){
        connectedServerNodes.remove(protocolInfo);
        rpcProtocolSet.remove(protocolInfo);
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            log.warn("Waiting for available service");
            return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        isRunning = false;
        for (ProtocolInfo protocolInfo : rpcProtocolSet) {
            removeServiceInfo(protocolInfo);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }


}
