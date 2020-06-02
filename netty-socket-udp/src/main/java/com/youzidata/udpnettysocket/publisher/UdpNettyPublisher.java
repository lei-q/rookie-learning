package com.youzidata.udpnettysocket.publisher;

import com.youzidata.udpnettysocket.service.BroadcastManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;

@Component
public class UdpNettyPublisher {

    private Channel channel;

    @Autowired
    public UdpNettyPublisher(Channel channel) {
        this.channel = channel;
    }

    /**
     * 指定主题发送
     *
     * @param topic  主题
     * @param record 消息
     * @throws Exception
     */
    public void send(String topic, String record) {

        BroadcastManager.getTopicInet(topic).forEach((k, v) -> {
            try {
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                        record, CharsetUtil.UTF_8), v)).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 指定ip port 发送
     *
     * @param inetSocketAddress 网址
     * @param record            消息
     * @throws Exception
     */
    public void send(InetSocketAddress inetSocketAddress, String record) throws Exception {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                record, CharsetUtil.UTF_8), inetSocketAddress)).sync();
    }

}
