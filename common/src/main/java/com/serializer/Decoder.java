package com.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Decoder extends ByteToMessageDecoder {

    private CusomSerializer cusomSerializer;
    private Class<?> serializeClass;

    public Decoder(CusomSerializer cusomSerializer, Class<?> serializeClass) {
        this.cusomSerializer = cusomSerializer;
        this.serializeClass = serializeClass;
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj;
        try {
            obj = cusomSerializer.deserialize(data, serializeClass);
            list.add(obj);
        } catch (Exception ex) {
            log.error("Decode error: " + ex);
        }
    }
}
