package com.handler;

import com.pojo.Beat;
import com.pojo.RpcResp;
import com.utils.ServiceKeyUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.pojo.RpcReq;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcReq> {

    private Map<String,Object> serviceObjectMap;
    private ThreadPoolExecutor serverHandlerPool;


    public RpcServerHandler(Map<String, Object> serviceObjectMap, ThreadPoolExecutor serverHandlerPool) {
        this.serviceObjectMap = serviceObjectMap;
        this.serverHandlerPool = serverHandlerPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcReq msg) throws Exception {
        if (msg.getReqUid().equals(Beat.PING)){
            log.info("beat req -> ping");
            serverHandlerPool.execute(() ->{
                ctx.writeAndFlush(Beat.PONG_REQ).addListener((ChannelFutureListener) channelFuture -> {
                    log.info("server recv request id:{}",msg.getReqUid());
                });
            });

        }else{
            serverHandlerPool.execute(() ->{
                final RpcResp rpcResp = new RpcResp();
                try {
                    rpcResp.setReqUid(msg.getReqUid());
                    final Object o = methodInvoke(msg);
                    rpcResp.setResult(o);
                }catch (Exception e){
                    rpcResp.setError(e.getMessage());
                }
                ctx.writeAndFlush(rpcResp).addListener((ChannelFutureListener) channelFuture -> {
                    log.info("server recv request id:{}",msg.getReqUid());
                });
            });
        }
    }

    private Object methodInvoke(RpcReq rpcReq) throws InvocationTargetException {

        final String methodName = rpcReq.getMethodName();
        final String className = rpcReq.getClassName();
        final String version = rpcReq.getVersion();
        final Class<?>[] paramTypes = rpcReq.getParamTypes();
        final Object[] paramValues = rpcReq.getParamValues();

        final Object serviceBean = serviceObjectMap.get(ServiceKeyUtil.getkey(className, version));
        final Class<?> serviceBeanClass = serviceBean.getClass();


        final FastClass fastClass = FastClass.create(serviceBeanClass);
        final int index = fastClass.getIndex(methodName, paramTypes);

        return fastClass.invoke(index,serviceBean,paramValues);
    }
}
