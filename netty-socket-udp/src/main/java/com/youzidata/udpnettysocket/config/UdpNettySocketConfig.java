package com.youzidata.udpnettysocket.config;

import com.youzidata.udpnettysocket.initializer.UdpServerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class UdpNettySocketConfig {

    @Bean
    public Bootstrap bootstrap() {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        //引导该 NioDatagramChannel（无连接的）
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                //设置 SO_BROADCAST 套接字选项
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UdpServerInitializer());

        return bootstrap;
    }

    @Bean
    public Channel channel(Bootstrap bootstrap) throws Exception {

        System.out.println("--------- UDP SERVER START ------------");

        return bootstrap.bind(8887).sync().channel();
    }
}
