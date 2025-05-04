package com.joopro.Joosik_Pro.service.StockService;

import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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

    private static final int RECONNECT_DELAY_MS = 3000; // 3초 후 재연결
    private static final int MAX_RECONNECT_ATTEMPTS = 5; // 최대 재시도 5번

    private int reconnectAttempts = 0;
    private String currentStockCode; // 현재 구독 중인 종목코드 저장


    public void startLiveStream(String stockCode) {
        this.currentStockCode = stockCode;
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            URI uri = new URI("ws://ops.koreainvestment.com:31000/tryitout/H0STCNT0");

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("WebSocket 연결");
                    reconnectAttempts = 0; // 연결 성공하면 재시도 횟수 초기화

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
                            """, approvalKey, currentStockCode
                    );

                    send(request);
                    log.info("실시간 시세 요청 전송 완료: {}", currentStockCode);
                }

                @Override
                public void onMessage(String message) {
                    log.info("수신된 실시간 메시지: {}", message);
                    try {
                        String[] parts = message.split("\\^");

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
                    attemptReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 에러 발생", ex);
                    attemptReconnect();
                }
            };

            client.connect();

        } catch (Exception e) {
            log.error("WebSocket 연결 중 예외 발생", e);
            attemptReconnect();
        }
    }

    @Async
    protected void attemptReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            log.info("WebSocket 재연결 시도 ({} / {})", reconnectAttempts, MAX_RECONNECT_ATTEMPTS);

            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                connectWebSocket(); // 다시 연결 시도
            } catch (InterruptedException e) {
                log.error("재연결 대기 중 인터럽트 발생", e);
                Thread.currentThread().interrupt();
            }
        } else {
            log.error("WebSocket 최대 재연결 시도 초과. 연결 포기");
        }
    }

    public void stopLiveStream() {
        if (client != null && client.isOpen()) {
            client.close();
            log.info("WebSocket 연결 종료 요청");
        }
    }
}
