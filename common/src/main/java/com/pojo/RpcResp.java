package com.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResp implements Serializable {


    private static final long serialVersionUID = -5631580990151971192L;


    private String reqUid;
    private String error;
    private Object result;

    public boolean isError(){return error != null; }
    public String getError(){return error; }



}
