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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Async를 붙여서 메인 쓰레드가 멈추지 않고 재연결 시도를 백그라운드에서 별도 쓰레드로 작업
 * @Async -> 해당 메서드를 별도 스레드에서 비동기로 실행하라는 의미
 * 하지만 여전히 @Async메서드에서 Trhead.sleep()를 사용함으로써 스레드 낭비 가능성 존재
 * Thread.sleep(3000)이 들어가면 그 새 스레드는 3초 동안 멈춤
 *
 * ScheduledExecutorService.schedule() 는 논블로킹 방식
 * 현재 스레드는 즉시 다음 코드로 넘억감, 스케줄러가 내부 스레드를 꺼내서 작업 수행
 * ScheduledExecutorService : 비동기 지연 스케줄러, Nonblocking 방식 채택
 *
 * 이미 웹소켓이 연결되어 있을 때 중복 연결 시키는 경우를 막기 위해서
 * 연결되어 있는 웹소켓 관리, ConcurrentHashMap을 이용하여 관리
 *
 */

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

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private static final int RECONNECT_DELAY_MS = 3000; // 3초 후 재연결
    private static final int MAX_RECONNECT_ATTEMPTS = 5; // 최대 재시도 5번

    private final Map<String, WebSocketClient> clientMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> reconnectAttempts = new ConcurrentHashMap<>();

    public void startLiveStream(String stockCode) {
        if(clientMap.containsKey(stockCode)) {
            log.info("이미 구독 중인 종목입니다 : {}", stockCode);
            return;
        }
        connectWebSocket(stockCode);
    }

    private void connectWebSocket(String stockCode) {
        try {
            URI uri = new URI("ws://ops.koreainvestment.com:31000/tryitout/H0STCNT0");

            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("WebSocket 연결");
                    reconnectAttempts.put(stockCode, 0); // 연결 성공하면 재시도 횟수 초기화

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
                            """, approvalKey, stockCode);

                    send(request);
                }

                @Override
                public void onMessage(String message) {
                    log.info("실시간 수신 [{}]: {}", stockCode, message);
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
                    scheduleReconnect(stockCode);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 에러 발생", ex);
                    scheduleReconnect(stockCode);
                }
            };

            client.connect();

        } catch (Exception e) {
            log.error("WebSocket 연결 중 예외 발생", e);
            scheduleReconnect(stockCode);
        }
    }

    @Async
    protected void scheduleReconnect(String stockCode) {
        int attempts = reconnectAttempts.getOrDefault(stockCode, 0);

        if (attempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts.put(stockCode, attempts + 1);

            scheduler.schedule(()-> connectWebSocket(stockCode),
                    RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            log.error("WebSocket 최대 재연결 시도 초과. 연결 포기");
            clientMap.remove(stockCode);
        }
    }

    public void stopLiveStream(String stockCode) {
        WebSocketClient client = clientMap.get(stockCode);
        if (client != null && client.isOpen()) {
            client.close();
            log.info("WebSocket 수동 종료 요청 [{}]", stockCode);
        }
        clientMap.remove(stockCode);
        reconnectAttempts.remove(stockCode);
    }

    public void stopAllStreams() {
        clientMap.forEach((code, client) -> {
            if (client.isOpen()) client.close();
        });
        clientMap.clear();
        reconnectAttempts.clear();
        log.info("모든 WebSocket 스트림 종료");
    }

}
