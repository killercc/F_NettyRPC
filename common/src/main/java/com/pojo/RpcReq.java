package com.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcReq implements Serializable {

    private static final long serialVersionUID = -8204415925881647322L;


    private String reqUid;
    private String className;
    private String methodName;
    private String version;
    private Class<?>[] paramTypes;
    private Object[] paramValues;

}
