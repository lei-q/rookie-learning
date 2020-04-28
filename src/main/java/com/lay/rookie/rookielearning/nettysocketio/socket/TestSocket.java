package com.lay.rookie.rookielearning.nettysocketio.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.youzidata.api.nettysocketio.AbstractNettySocket;
import com.youzidata.api.nettysocketio.Message;
import com.youzidata.dto.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TestSocket extends AbstractNettySocket {

    /**
     * 加入启动计划
     *
     * @throws Exception
     */
    @PostConstruct
    private void addStartUP() throws Exception {
        start();
    }

    /**
     * 命名空间
     */
    private static String NAMESPACE;

    @Override
    public String namespace() {
        TestSocket.NAMESPACE = "/socketio/testsocket";
        return TestSocket.NAMESPACE;
    }

    @Override
    public void start() throws Exception {
        /*
         * 消息事件
         */
        socketIONamespace().addEventListener(MESSAGE, Message.class, (client, data, ackSender) -> {
            pushMessage(new Message(data.getToken(), data.getContent()));
        });

        super.start();
    }

    @Override
    public Message connectPushMessage() {
        Message message = new Message();
        message.setContent(Result.created());
        return message;
    }

    public static void pushMessage(Message message) {
        String userUuid;
        if (StringUtils.isNotBlank(userUuid = message.getToken())) {
            SocketIOClient socketIOClient;
            if ((socketIOClient = clientMap.get(NAMESPACE).get(userUuid)) != null) {
                socketIOClient.sendEvent(MESSAGE, message.getContent());
            }
        }
    }
}
