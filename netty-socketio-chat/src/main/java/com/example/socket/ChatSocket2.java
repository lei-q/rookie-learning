package com.example.socket;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ChatSocket2 {

    private static final Logger logger = LoggerFactory.getLogger(ChatSocket2.class);

    @Autowired
    private SocketIOServer socketIOServer;

    private SocketIONamespace namespace;

    /**
     * 加入启动计划
     */
    @PostConstruct
    private void addStartUP() {
        this.namespace = socketIOServer.addNamespace("/chat2");
        start();
    }

    /**
     * 自定义事件名
     */
    private static final String MESSAGE = "message";

    private static final String INIT = "init";


    public void start() {
        /*
         * 监听连接事件
         */
        this.namespace.addConnectListener(client -> {
            logger.info("连接成功");
            logger.info(client.getSessionId() + "");
        });

        /*
         * 监听关闭事件
         */
        this.namespace.addDisconnectListener(client -> logger.info("断开连接"));

        /*
         * 监听自定义事件
         */
        this.namespace.addEventListener(MESSAGE, MsgObject.class, (client, data, ackSender) -> logger.info("收到消息：" + data));

        this.namespace.addEventListener(INIT, String.class, (client, data, ackSender) -> logger.info("收到消息：" + data));

    }
}
