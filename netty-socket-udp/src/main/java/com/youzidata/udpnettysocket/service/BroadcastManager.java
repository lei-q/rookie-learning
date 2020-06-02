package com.youzidata.udpnettysocket.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastManager {

    public static final String SEPARATOR = "::";

    /**
     * 记录订阅主题的 网址
     * key(topic): value(key(ip::port) : value(InetSocketAddress))
     */
    public static Map<String, Map<String, InetSocketAddress>> topicInet = new ConcurrentHashMap<>();

    public static void putTopicInet(String topic, InetSocketAddress inetSocketAddress) {
        Map<String, InetSocketAddress> inetSocketAddressMap = Optional.ofNullable(BroadcastManager.topicInet.get(topic)).orElse(new HashMap<>());
        inetSocketAddressMap.put(joinKey(inetSocketAddress.getHostString(), inetSocketAddress.getPort()), inetSocketAddress);
        BroadcastManager.topicInet.put(topic, inetSocketAddressMap);
    }

    public static Map<String, InetSocketAddress> getTopicInet(String topic) {
        return Optional.ofNullable(BroadcastManager.topicInet.get(topic)).orElse(new HashMap<>());
    }

    public static void removeTopicInet(InetSocketAddress inetSocketAddress, String topic) {
        Map<String, InetSocketAddress> inetSocketAddressMap = BroadcastManager.topicInet.get(topic);
        if (inetSocketAddressMap != null) {
            inetSocketAddressMap.remove(joinKey(inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        }
    }

    public static void removeTopicInetByInetSocketAddress(InetSocketAddress inetSocketAddress) {
        Map<String, Map<String, InetSocketAddress>> topicInet = BroadcastManager.topicInet;
        topicInet.forEach((k, v) -> v.remove(inetSocketAddress));
    }


    /**
     * 记录心跳连接的 网址
     * key(ip::port) : value(InetSocketAddress)
     */
    public static Map<String, InetSocketAddress> heartbeatInet = new ConcurrentHashMap<>();

    public static void putHeartbeatInet(InetSocketAddress inetSocketAddress) {
        BroadcastManager.heartbeatInet.put(joinKey(inetSocketAddress.getHostString(), inetSocketAddress.getPort()), inetSocketAddress);
    }

    public static InetSocketAddress getHeartbeatInet(String ip, int port) {
        return BroadcastManager.heartbeatInet.get(joinKey(ip, port));
    }

    public static void removeHearbeatInet(InetSocketAddress inetSocketAddress) {
        BroadcastManager.heartbeatInet.remove(joinKey(inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
    }


    private static String joinKey(String ip, int port) {
        return ip + SEPARATOR + port;
    }
}
