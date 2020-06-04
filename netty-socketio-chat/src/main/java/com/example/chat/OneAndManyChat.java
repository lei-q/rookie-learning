package com.example.chat;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class OneAndManyChat {

    private static final Logger logger = LoggerFactory.getLogger(OneAndManyChat.class);

    @Autowired
    private SocketIOServer socketIOServer;

    private SocketIONamespace namespace;

    /**
     * 加入启动计划
     */
    @PostConstruct
    private void addStartUP() {
        this.namespace = socketIOServer.addNamespace("/oneAndManyChat");
        start();
    }

    public void start() {
        /*
         * 监听连接事件
         */
        this.namespace.addConnectListener(client -> logger.info(client.getSessionId() + " : 连接成功"));

        /*
         * 监听关闭事件
         */
        this.namespace.addDisconnectListener(client -> logger.info(client.getSessionId() + " : 断开连接"));

        /*
         * 监听自定义事件
         */
        this.namespace.addEventListener("chatOne", Msg.class, (client, data, ackSender) -> {
            System.out.println("一对一" + client.getSessionId());
            // chatevent为 事件的名称， data为发送的内容
            this.namespace.getClient(client.getSessionId()).sendEvent("chatOne", data);
        });

        this.namespace.addEventListener("chatMany", Msg.class, (client, data, ackSender) -> {
            System.out.println("一对多" + client.getSessionId());
            // chatevent为 事件的名称， data为发送的内容
            this.namespace.getBroadcastOperations().sendEvent("chatMany", client);
        });
    }
}