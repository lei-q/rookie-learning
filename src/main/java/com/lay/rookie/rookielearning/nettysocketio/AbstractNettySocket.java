package com.lay.rookie.rookielearning.nettysocketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义socket抽象类
 */
public abstract class AbstractNettySocket {

    /**
     * 客户端 <namespace, <token, SocketIOClient>>
     */
    protected static ConcurrentHashMap<String, ConcurrentHashMap<String, SocketIOClient>> clientMap = new ConcurrentHashMap<>();

    @Autowired
    private SocketIOServer socketIOServer;

    /**
     * 命名空间
     *
     * @return
     */
    public abstract String namespace();

    protected SocketIONamespace socketIONamespace() {
        return socketIOServer.addNamespace(namespace());
    }

    /**
     * 消息事件
     */
    public static final String MESSAGE = "message";

    public static final String TOKEN = "token";

    /**
     * 启动计划
     *
     * @throws Exception
     */
    public void start() throws Exception {
        /*
         * 监听连接事件
         */
        socketIONamespace().addConnectListener(client -> {
            String token;
            if (StringUtils.isNotBlank(token = client.getHandshakeData().getSingleUrlParam(TOKEN))) {
                if (clientMap.containsKey(namespace())) {
                    clientMap.get(namespace()).put(token, client);
                } else {
                    ConcurrentHashMap<String, SocketIOClient> map = new ConcurrentHashMap<>();
                    map.put(token, client);
                    clientMap.put(namespace(), map);
                }
                System.out.println(clientMap);
            }
            client.sendEvent(MESSAGE, connectPushMessage().getContent());
            System.out.println(String.format("%s, %s 连接成功", token, client.getNamespace().getName()));
        });

        /*
         * 监听关闭事件
         */
        socketIONamespace().addDisconnectListener(client -> {
            String token = client.getHandshakeData().getSingleUrlParam(TOKEN);
            if (!clientMap.isEmpty()) {
                if (clientMap.get(namespace()) != null) {
                    if (clientMap.get(namespace()).get(token) != null) {
                        clientMap.get(namespace()).remove(token);
                    }
                }
                if (clientMap.get(namespace()).isEmpty()) {
                    clientMap.remove(namespace());
                }
            }
            client.disconnect();
            System.out.println(String.format("%s, %s 连接断开", token, client.getNamespace().getName()));
        });
    }

    /**
     * 连接成功推送数据
     */
    public abstract Message connectPushMessage();
}
