package com.pojo;

import com.utils.JsonUtil;
import lombok.Data;

@Data
public class ServiceInfo {
    private static final long serialVersionUID = 109501380685543513L;

    private String servicever;
    private String serviceName;
    private Object serviceBean;

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }
    public int RehashCode(){
        return this.hashCode() & Integer.MAX_VALUE;
    }

}
