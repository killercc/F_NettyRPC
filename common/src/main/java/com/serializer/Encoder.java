package com.serializer;

import com.pojo.RpcResp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Encoder extends MessageToByteEncoder {

    private CusomSerializer cusomSerializer;
    private Class<?> serializeClass;

    public Encoder(CusomSerializer cusomSerializer, Class<?> serializeClass) {
        this.cusomSerializer = cusomSerializer;
        this.serializeClass = serializeClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object rpcResp, ByteBuf byteBuf) throws Exception {
        if (serializeClass.isInstance(rpcResp)) {
            try {
                byte[] data = cusomSerializer.serialize(rpcResp);
                byteBuf.writeInt(data.length);
                byteBuf.writeBytes(data);
            } catch (Exception ex) {
                log.error("Encode error: " + ex);
            }
        }
    }
}
