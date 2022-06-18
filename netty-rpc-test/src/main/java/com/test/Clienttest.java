package com.test;

import com.RpcClient;
import com.manager.ConnectionManager;
import com.service.Hello;

import java.util.TreeMap;

public class Clienttest {
    private static RpcClient rpcClient;
    public static void main(String[] args) throws Exception {
        rpcClient = new RpcClient("124.221.233.199:2181");
        benchmark();

    }
    private static void heartbeattest() throws InterruptedException {
        while (true){
//            final Hello service = rpcClient.createService(Hello.class, "1.0");
//            String result = service.hi("test ping");
//            System.out.println("result = " + result);
            Thread.sleep(100);
        }
    }

    private static void benchmark() throws Exception {
        // 测试线程数
        int threadNum = 1;
        // 测试请求数
        final int requestNum = 19000;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {

                        final Hello service = rpcClient.createService(Hello.class, "1.0");
                        String result = service.hi(Integer.toString(i1));
                        System.out.println("result = " + result);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("total-time-cost:%s ms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);
        rpcClient.stop();

    }
}
