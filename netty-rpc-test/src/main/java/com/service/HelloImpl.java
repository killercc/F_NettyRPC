package com.service;

import com.annotation.NettyRpcService;

@NettyRpcService(value = Hello.class, version = "1.0")
public class HelloImpl implements Hello
{
    @Override
    public String hi(String index) {
        return "Hi Netty RPC " + index;
    }
}
