package com.joopro.Joosik_Pro.service.StockService;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Slf4j
public class DomesticStockLiveService {

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
                    // DB 저장 로직 추가
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
