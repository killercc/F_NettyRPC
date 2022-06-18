package com.pojo;

import com.utils.JsonUtil;
import lombok.Data;

import java.util.List;

@Data
public class ProtocolInfo {

    private String host;

    private int port;

    private List<ServiceInfo> serviceInfoList;

    public String toJson(){
        return JsonUtil.objectToJson(this);
    }

    public static ProtocolInfo fromJson(String json){
        return JsonUtil.jsonToObject(json,ProtocolInfo.class);
    }

    public int RehashCode(){
        return this.hashCode() & Integer.MAX_VALUE;
    }

}
