package com.joopro.Joosik_Pro.service.StockService;

import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;

@Service
@Slf4j
public class DomesticStockLiveService {

    @Autowired
    WriteApi writeApi;

    @Value("${koreainvest.appkey}")
    private String appKey;

    @Value("${koreainvest.appsecret}")
    private String appSecret;

    @Value("${koreainvest.auth.token}")
    private String approvalKey;

    private WebSocketClient client;

    public void startLiveStream(String stockCode) {
        try {
            URI uri = new URI("ws://ops.koreainvestment.com:31000/tryitout/H0STCNT0");

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("WebSocket 연결");

                    String request = String.format(
                            """
                            {
                              "header": {
                                "approval_key": "%s",
                                "custtype": "P",
                                "tr_type": "1",
                                "content-type": "utf-8"
                              },
                              "body": {
                                "input": {
                                  "tr_id": "H0STCNT0",
                                  "tr_key": "%s"
                                }
                              }
                            }
                            """, approvalKey, stockCode
                    );

                    send(request);
                    log.info("실시간 시세 요청 전송 완료: {}", stockCode);
                }

                @Override
                public void onMessage(String message) {
                    log.info("수신된 실시간 메시지: {}", message);
                    try {
                        // 예: "005930^092210^56650^2^450^0.80^..."
                        String[] parts = message.split("\\^");
                        for(String go : parts){
                            System.out.println(go);
                        }

                        String stockCode = parts[0];
                        long currentPrice = Long.parseLong(parts[2]);
                        long volume = Long.parseLong(parts[13]);

                        Point point = Point
                                .measurement("stock_price")
                                .addTag("code", stockCode)
                                .addField("price", currentPrice)
                                .addField("volume", volume)
                                .time(Instant.now(), WritePrecision.MS);

                        writeApi.writePoint(point);
                        log.info("InfluxDB에 저장 완료: {}", stockCode);

                    } catch (Exception e) {
                        log.error("메시지 파싱 또는 InfluxDB 저장 중 오류", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("WebSocket 종료 - Code: {}, Reason: {}", code, reason);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 에러 발생", ex);
                }
            };

            client.connect();

        } catch (Exception e) {
            log.error("WebSocket 연결 중 예외 발생", e);
        }
    }

    public void stopLiveStream() {
        if (client != null && client.isOpen()) {
            client.close();
            log.info("WebSocket 연결 종료 요청");
        }
    }
}
