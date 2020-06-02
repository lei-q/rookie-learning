package com.youzidata.udpnettysocket.handler;

import com.youzidata.udpnettysocket.dto.ASMGCSDto;
import com.youzidata.udpnettysocket.service.BroadcastManager;
import com.youzidata.xml.Dom4jHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

        String content = packet.content().toString(CharsetUtil.UTF_8);

        InetSocketAddress inetSocketAddress = packet.sender();

        System.out.println(inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort() + " --- " + content);

        ASMGCSDto asmgcsDto = Dom4jHandler.xmlToObject(content, ASMGCSDto.class);

        String titleT = asmgcsDto.getHead().getTitle();

        ASMGCSDto backMsg = new ASMGCSDto();

        // 收到订阅请求消息
        if ("SUBSCRIBE".equals(titleT) || "RESUBSCRIBE".equals(titleT)) {
            backMsg.getHead().setTitle("SUBREP");
            try {
                subscribe(asmgcsDto.getBody().getTitles_subscribe(), inetSocketAddress);
                backMsg.getBody().setSubrep("SUCCESSFULLY");
            } catch (Exception e) {
                backMsg.getBody().setSubrep("FAILED");
                backMsg.getBody().setError(201);
                backMsg.getBody().setReason("Subscribe is already exist.");
                e.printStackTrace();
            }
        }

        // 收到取消订阅消息
        if ("UNSUBSCRIBE".equals(titleT)) {
            backMsg.getHead().setTitle("UNSUBREP");
            try {
                unsubscribe(asmgcsDto.getBody().getTitles_subscribe(), inetSocketAddress);
                backMsg.getBody().setSubrep("SUCCESSFULLY");
            } catch (Exception e) {
                backMsg.getBody().setSubrep("FAILED");
                backMsg.getBody().setError(203);
                backMsg.getBody().setReason("Subscribe is not active.");
                e.printStackTrace();
            }
        }

        // 收到结束订阅消息
        if ("SUBEND".equals(titleT)) {
            backMsg.getHead().setTitle("SUBEND");
            subend(inetSocketAddress);
        }

        content = Dom4jHandler.xmlToString(Dom4jHandler.createDocument(backMsg));

        // 消息发送...
        DatagramPacket dp = new DatagramPacket(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8), packet.sender());

        ctx.writeAndFlush(dp);

    }

    /**
     * 订阅
     *
     * @param topic             主题
     * @param inetSocketAddress 网址
     */
    public void subscribe(String topic, InetSocketAddress inetSocketAddress) {

        BroadcastManager.putHeartbeatInet(inetSocketAddress);

        BroadcastManager.putTopicInet(topic, inetSocketAddress);

    }

    /**
     * 取消订阅
     *
     * @param inetSocketAddress 网址
     * @param topic             主题
     */
    public void unsubscribe(String topic, InetSocketAddress inetSocketAddress) {
        BroadcastManager.removeTopicInet(inetSocketAddress, topic);
    }

    /**
     * 结束订阅
     *
     * @param inetSocketAddress 网址
     */
    public void subend(InetSocketAddress inetSocketAddress) {
        BroadcastManager.removeHearbeatInet(inetSocketAddress);

        BroadcastManager.removeTopicInetByInetSocketAddress(inetSocketAddress);

    }
}