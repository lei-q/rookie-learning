package com.youzidata.udpnettysocket.initializer;

import com.youzidata.udpnettysocket.handler.UdpServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * netty udp 初始化器
 */
public class UdpServerInitializer extends
        ChannelInitializer<NioDatagramChannel> {

    @Override
    public void initChannel(NioDatagramChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new UdpServerHandler());//消息处理器

    }

}