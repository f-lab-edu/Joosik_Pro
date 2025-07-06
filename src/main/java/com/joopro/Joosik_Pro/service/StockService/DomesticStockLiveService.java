package com.joopro.Joosik_Pro.service.StockService;

import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.joopro.Joosik_Pro.dto.StockMessage;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

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
 * stopLiveStream,, startLiveStream에서 symbolToClient를 확인하는 과정에서 동시성 이슈 발생 가능성 존재
 * 메서드에 synchronized를 붙이더라도 stopLivestream과 startLivestream을 동시에 돌리면 존재 유무를 확인하고 넘어가는 동안 map에 있는 데이터 삭제 가능
 * -> ReentrantLock 사용
 *
 */

@Service
@Slf4j
public class DomesticStockLiveService {

    @Autowired
    WriteApi writeApi;

//    @Autowired private KafkaStockProducer kafkaStockProducer;

    @Value("${koreainvest.appkey}")
    private String appKey;

    @Value("${koreainvest.appsecret}")
    private String appSecret;

    @Value("${koreainvest.auth.approvalkey}")
    private String approvalKey;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private static final int RECONNECT_DELAY_MS = 3000; // 3초 후 재연결
    private static final int MAX_RECONNECT_ATTEMPTS = 5; // 최대 재시도 5번

    private static final int MAX_SYMBOLS_PER_SOCKET = 40; // 소켓 당 최대 40개

    private final ReentrantLock lock = new ReentrantLock();

    private final List<WebSocketClient> clients = new CopyOnWriteArrayList<>(); // 웹소켓 목록
    private final Map<WebSocketClient, Set<String>> clientSymbolsMap = new ConcurrentHashMap<>(); // 웹소켓 당 연결되어 있는 종목 set
    private final Map<String, WebSocketClient> symbolToClient = new ConcurrentHashMap<>(); // 각 종목이 어디 웹소켓에 연결되어 있는지 확인하는 맵
    private final Map<WebSocketClient, Integer> reconnectAttempts = new ConcurrentHashMap<>(); // 웹소켓 끊겼을 때 재연결 시도 횟수

    public void startLiveStream(String stockCode) {
        lock.lock();
        try{
            if(symbolToClient.containsKey(stockCode)) {
                log.info("이미 구독 중인 종목입니다 : {}", stockCode);
                return;
            }
            WebSocketClient assignedClient = null;
            for(WebSocketClient webSocketClient : clients){
                if(clientSymbolsMap.get(webSocketClient).size() < MAX_SYMBOLS_PER_SOCKET){
                    assignedClient = webSocketClient;
                    break;
                }
            }
            if (assignedClient == null) {
                assignedClient = createNewWebSocketClient();
                clients.add(assignedClient);
                clientSymbolsMap.put(assignedClient, ConcurrentHashMap.newKeySet());
                reconnectAttempts.put(assignedClient, 0);
                assignedClient.connect();
            }
            clientSymbolsMap.get(assignedClient).add(stockCode);
            symbolToClient.put(stockCode, assignedClient);

        }
        finally{
            lock.unlock();
        }
    }

    private WebSocketClient createNewWebSocketClient() {
        try {
            URI uri = new URI("ws://ops.koreainvestment.com:31000/tryitout/H0STCNT0");

            return new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("WebSocket 연결 성공");
                    reconnectAttempts.put(this, 0);

                    Set<String> symbols = clientSymbolsMap.get(this);
                    for (String code : symbols) {
                        sendSubscribeRequest(this, code);
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {
                        log.info("사이즈는? : {}", symbolToClient.size());
                        log.info("이렇게 온다 : {}", message);

                        // 1. JSON 메시지 처리 (오류, 승인, 핑퐁 등)
                        if (message.trim().startsWith("{")) {

//                            JsonNode json = objectMapper.readTree(message);
//                            String trId = json.path("header").path("tr_id").asText();
//
//                            if ("PINGPONG".equals(trId)) {
//                                // 이걸 그냥 서버에 다시 보내줌
//                                log.info("PINGPONG 수신 → JSON 그대로 서버에 전송");
//                                sendSubscribeRequest(this, "035420");
//                                return;
//                            }

                            log.info("JSON 제어 메시지 수신: {}", message);
                            // 필요한 경우 JSON 파싱해서 로그 남기거나 처리
                            return;
                        }

                        // 2. 실시간 시세 데이터 처리
                        // 예: 0|H0STCNT0|001|005930^154651^63800^2^3000^4.93...
                        String[] outerParts = message.split("\\|");
                        if (outerParts.length < 4) {
                            log.warn("예상치 못한 메시지 형식(outerParts): {}", message);
                            return;
                        }

                        String dataPart = outerParts[3];
                        String[] parts = dataPart.split("\\^");
                        if (parts.length < 14) {
                            log.warn("예상치 못한 메시지 형식(parts): {}", message);
                            return;
                        }

                        StockMessage stockMessage = new StockMessage(
                                parts[0],                        // code
                                parts[1],                        // name (timestamp string?)
                                Long.parseLong(parts[2]),       // price
                                Instant.now().toString()        // timestamp
                        );

                        log.info("stockMessageName : {}, stockMessagePrice : {}", stockMessage.getName(), stockMessage.getPrice());

                        // kafkaStockProducer.sendStockData(stockMessage);

                        log.info("Kafka로 JSON 메시지 전송 완료: {}", stockMessage);
//                        String[] parts = message.split("\\^");
//                        StockMessage stockMessage = new StockMessage(
//                                parts[0],                        // code
//                                parts[1],                        // name
//                                Long.parseLong(parts[2]),       // price
//                                Instant.now().toString()        // timestamp
//                        );
//                        log.info("stockMessageName : {}, stockMessagePrice : {}", stockMessage.getName(), stockMessage.getPrice());

//                        kafkaStockProducer.sendStockData(stockMessage);
                    } catch (Exception e) {
                        log.error("메시지 파싱 또는 저장 오류", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("WebSocket 종료 - Code: {}, Reason: {}", code, reason);
                    scheduleReconnect(this);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 에러 발생", ex);
                    scheduleReconnect(this);
                }
            };

        } catch (Exception e) {
            throw new RuntimeException("WebSocketClient 생성 실패", e);
        }
    }

    private void sendSubscribeRequest(WebSocketClient client, String stockCode) {
        String request = String.format("""
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
        client.send(request);
        log.info("구독 요청 보냄: {}", stockCode);
    }


    @Async
    protected void scheduleReconnect(WebSocketClient client) {
        int attempts = reconnectAttempts.getOrDefault(client, 0);

        if (attempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts.put(client, attempts + 1);
            scheduler.schedule(() -> {
                log.info("WebSocket 재연결 시도...");
                client.reconnect();
            }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            log.error("재연결 최대 시도 초과. 이 WebSocket은 더 이상 사용되지 않음");
            Set<String> symbols = clientSymbolsMap.remove(client);
            if (symbols != null) {
                symbols.forEach(symbolToClient::remove);
            }
            clients.remove(client);
            reconnectAttempts.remove(client);
        }
    }

    public void stopLiveStream(String stockCode) {
        lock.lock();
        try{
            WebSocketClient client = symbolToClient.remove(stockCode);
            if (client != null) {
                Set<String> symbols = clientSymbolsMap.get(client);
                if (symbols != null) {
                    symbols.remove(stockCode);
                    log.info("구독 중단: {}", stockCode);
                }

                // 소켓에 더 이상 종목이 없으면 소켓 종료
                if (symbols != null && symbols.isEmpty()) {
                    client.close();
                    clients.remove(client);
                    clientSymbolsMap.remove(client);
                    reconnectAttempts.remove(client);
                    log.info("WebSocket 연결 종료 (더 이상 구독 없음)");
                }
            }
        }finally {
            lock.unlock();
        }
    }
}
