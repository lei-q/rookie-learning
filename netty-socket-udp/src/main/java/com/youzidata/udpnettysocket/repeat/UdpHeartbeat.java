package com.youzidata.udpnettysocket.repeat;

import com.youzidata.udpnettysocket.dto.ASMGCSDto;
import com.youzidata.udpnettysocket.publisher.UdpNettyPublisher;
import com.youzidata.udpnettysocket.service.BroadcastManager;
import com.youzidata.xml.Dom4jHandler;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

/**
 * 向订阅方发送心跳
 */
@Component
@Order(1)
public class UdpHeartbeat implements CommandLineRunner {

    @Autowired
    public UdpNettyPublisher udpNettyPublisher;

    @Override
    public void run(String... args) throws Exception {

        String stringHEARTBEAT = getStringHEARTBEAT();
        CompletableFuture.runAsync(() -> {
            for (; ; ) {
                try {
                    BroadcastManager.heartbeatInet.forEach((k, inetSocketAddress) -> {
                        try {
                            udpNettyPublisher.send(inetSocketAddress, stringHEARTBEAT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getStringHEARTBEAT() throws IOException, IllegalAccessException, IntrospectionException, InvocationTargetException {

        ASMGCSDto asmgcsDto = new ASMGCSDto();
        asmgcsDto.getHead().setTitle("HEARTBEAT");

        Document document = Dom4jHandler.createDocument(asmgcsDto);

        return Dom4jHandler.xmlToString(document);
    }
}
