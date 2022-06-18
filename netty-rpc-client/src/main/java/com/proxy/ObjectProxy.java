package com.proxy;

import com.handler.RpcClientHandler;
import com.handler.RpcFuture;
import com.manager.ConnectionManager;
import com.pojo.RpcReq;
import com.utils.ServiceKeyUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
@Slf4j
public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {

    private Object object;
    private String version;

    public ObjectProxy(Object object, String version) {
        this.object = object;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcReq rpcReq = new RpcReq();
        rpcReq.setReqUid(UUID.randomUUID().toString());
        rpcReq.setMethodName(method.getName());
        rpcReq.setClassName(method.getDeclaringClass().getName());
        rpcReq.setParamTypes(method.getParameterTypes());
        rpcReq.setParamValues(args);
        rpcReq.setVersion(version);

        final String serverKey = ServiceKeyUtil.getkey(method.getDeclaringClass().getName(), version);
        RpcClientHandler rpcClientHandler = ConnectionManager.getInstance().chooseHandler(serverKey);
        final RpcFuture rpcFuture = rpcClientHandler.sendRequest(rpcReq);

        return rpcFuture.get();
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        return null;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        return null;
    }
}
