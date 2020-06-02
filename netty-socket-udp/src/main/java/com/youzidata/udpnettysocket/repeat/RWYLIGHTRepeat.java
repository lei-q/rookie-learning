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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 模拟向订阅方发送《灯光状态信息》
 */
@Component
@Order(2)
public class RWYLIGHTRepeat implements CommandLineRunner {

    @Autowired
    public UdpNettyPublisher udpNettyPublisher;

    @Override
    public void run(String... args) throws Exception {

        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 10000; i++) {
                try {
                    BroadcastManager.getTopicInet("RWYLIGHT").forEach((k, inetSocketAddress) -> {
                        try {
                            udpNettyPublisher.send(inetSocketAddress, getRWYLIGHT());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getRWYLIGHT() throws IllegalAccessException, IntrospectionException, InvocationTargetException, IOException {
        ASMGCSDto asmgcsDto = new ASMGCSDto();
        asmgcsDto.getHead().setTitle("RWYLIGHT");

        List<ASMGCSDto.Runway> runways = new ArrayList<>();
        runways.add(new ASMGCSDto.Runway("L01", "ON", "200", "正常", null));
        runways.add(new ASMGCSDto.Runway("L02", "ON", "200", "正常", null));
        asmgcsDto.getBody().setRwylight(new ASMGCSDto.Rwylight());
        asmgcsDto.getBody().getRwylight().setRunway(runways);

        Document document = Dom4jHandler.createDocument(asmgcsDto);

        return Dom4jHandler.xmlToString(document);
    }
}
