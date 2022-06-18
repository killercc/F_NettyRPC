package com.test;

import com.serializer.CusomSerializer;
import com.serializer.kryo.KryoSerializer;

import java.util.Arrays;

public class SerializeTest {
    public static void main(String[] args) {
        stringSerialize();
    }
    private static void classSerialize(){
        final SerializeClass serializeClass = new SerializeClass();
        final CusomSerializer cusomSerializer = new KryoSerializer();
        //序列化 将类转换为二进制流
        final byte[] serialize_bytes = cusomSerializer.serialize(serializeClass);
        System.out.println(serialize_bytes.getClass().getTypeName());
        // 反序列化
        SerializeClass deserialize = (SerializeClass)cusomSerializer.deserialize(serialize_bytes,SerializeClass.class);
        deserialize.hellotest();
    }
    private static void stringSerialize(){
        final CusomSerializer cusomSerializer = new KryoSerializer();
        //序列化 将类转换为二进制流
        final byte[] serialize_bytes = cusomSerializer.serialize("Hello World");
        System.out.println("序列化结果: " + Arrays.toString(serialize_bytes));
        // 反序列化
        String deserialize = (String)cusomSerializer.deserialize(serialize_bytes,String.class);
        System.out.println("反序列化结果: " + deserialize);
    }
}
