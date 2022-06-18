package com.pojo;

public class Beat {

    public static String PING = "BEAT_PING";
    public static String PONG = "BEAT_PONG";
    public static final int BEAT_INTERVAL = 6;
    public static int BEAT_TIMEOUT = BEAT_INTERVAL * 12;

    public static RpcReq PING_REQ;
    public static RpcReq PONG_REQ;
    static {
        PING_REQ = new RpcReq();
        PING_REQ.setReqUid(PING);

        PONG_REQ = new RpcReq();
        PONG_REQ.setReqUid(PONG);
    }



}
