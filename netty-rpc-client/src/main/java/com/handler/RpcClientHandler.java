package com.handler;

import com.manager.ConnectionManager;
import com.pojo.Beat;
import com.pojo.ProtocolInfo;
import com.pojo.RpcReq;
import com.pojo.RpcResp;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResp> {



    private ProtocolInfo protocolInfo;
    private volatile Channel channel;
    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResp rpcResp) throws Exception {
        String requestId = rpcResp.getReqUid();
        log.debug("Receive response: " + requestId);
        RpcFuture rpcFuture = pendingRPC.get(requestId);
        if (rpcFuture != null) {
            pendingRPC.remove(requestId);
            rpcFuture.done(rpcResp);
        } else {
            log.warn("Can not get pending response for request id: " + requestId);
        }
    }

    public void setProtocolInfo(ProtocolInfo protocolInfo){

        this.protocolInfo = protocolInfo;

    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //心跳检测
            sendRequest(Beat.PING_REQ);
            log.debug("Client send beat-ping ");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    public RpcFuture sendRequest(RpcReq rpcReq){
        RpcFuture rpcFuture = new RpcFuture(rpcReq);
        pendingRPC.put(rpcReq.getReqUid(), rpcFuture);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(rpcReq).sync();
            if (!channelFuture.isSuccess()) {
                log.error("Send request {} error", rpcReq.getReqUid());
            }
        } catch (InterruptedException e) {
            log.error("Send request exception: " + e.getMessage());
        }

        return rpcFuture;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeProtocol(protocolInfo);
    }
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }



}
