package com.handler;

import com.esotericsoftware.kryo.KryoSerializable;
import com.pojo.Beat;
import com.pojo.RpcReq;
import com.pojo.RpcResp;
import com.serializer.CusomSerializer;
import com.serializer.Decoder;
import com.serializer.Encoder;
import com.serializer.kryo.KryoSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerHandlerInit extends ChannelInitializer<SocketChannel> {

    private final Map<String,Object> serviceObjectMap;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerHandlerInit(Map<String, Object> serviceObjectMap, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceObjectMap = serviceObjectMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        CusomSerializer cusomSerializer = new KryoSerializer();
        ch.pipeline()
                .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(new Decoder(cusomSerializer, RpcReq.class))
                .addLast(new Encoder(cusomSerializer,RpcResp.class))
                .addLast(new RpcServerHandler(serviceObjectMap,threadPoolExecutor));
    }
}
