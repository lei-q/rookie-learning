package com.youzidata;

import com.youzidata.udpnettysocket.service.BroadcastManager;
import com.youzidata.udpnettysocket.publisher.UdpNettyPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class ApplicationTests {

    @Autowired
	public UdpNettyPublisher udpNettyPublisher;



	@Test
	void contextLoads() throws Exception {

        Map<String, InetSocketAddress> inetMap = new HashMap<>();
        inetMap.put("localhost::1111", new InetSocketAddress("localhost", 1111));
        BroadcastManager.topicInet.put("LIGHT", inetMap);

        CompletableFuture.runAsync(()->{
            for (int i = 0; i < 10000; i++) {
                try {
                    udpNettyPublisher.send("LIGHT", "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈");
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

	}

}
