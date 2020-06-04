package com.example.socket;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ChatSocket {

    private static final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    @Autowired
    private SocketIOServer socketIOServer;

    private SocketIONamespace namespace;

    /**
     * 加入启动计划
     */
    @PostConstruct
    private void addStartUP() {
        // 指定命名空间
        this.namespace = socketIOServer.addNamespace("/chat1");
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
        this.namespace.addConnectListener(client -> logger.info(client.getSessionId() + " : 连接成功"));

        /*
         * 监听关闭事件
         */
        this.namespace.addDisconnectListener(client -> logger.info(client.getSessionId() + " : 断开连接"));

        /*
         * 监听自定义事件
         */
        this.namespace.addEventListener(MESSAGE, MsgObject.class, (client, data, ackSender) -> {
            logger.info(client.getSessionId() + " : " + data.getNickName() + " ：" + data.getContent());
            String room = data.getRoom();
            if (room != null) {
                this.namespace.getRoomOperations(room).sendEvent(MESSAGE, data);
            } else {
                socketIOServer.getBroadcastOperations().sendEvent(MESSAGE, data.getContent());
            }
        });

        this.namespace.addEventListener(INIT, MsgObject.class, (client, data, ackSender) -> {
            logger.info(client.getSessionId() + " : " + data);
            String room = data.getRoom();
            if (room != null) {
                // 加入房间
                client.joinRoom(room);
                logger.info(data.getNickName() + "加入了群聊");
            }
        });

    }
}
