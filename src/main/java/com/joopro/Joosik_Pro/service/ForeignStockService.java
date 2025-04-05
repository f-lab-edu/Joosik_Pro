    package com.joopro.Joosik_Pro.service;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.joopro.Joosik_Pro.domain.ForeignStock;
    import com.joopro.Joosik_Pro.repository.ForeignStockRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.*;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;
    import org.springframework.web.util.UriComponentsBuilder;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class ForeignStockService {

        private final ForeignStockRepository foreignStockRepository;
        private RestTemplate restTemplate = new RestTemplate();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Value("${koreainvest.api.first.url}")
        private String apiUrl;

        @Value("${koreainvest.appkey}")
        private String appKey;

        @Value("${koreainvest.appsecret}")
        private String appSecret;

        @Value("${koreainvest.auth.token}")
        private String authToken;

        @Value("${koreainvest.first.tr_id}")
        private String tr_id;

        public ForeignStock fetchForeignStock(String symbol) {
            // API 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", "Bearer " + authToken);
            headers.set("appkey", appKey);
            headers.set("appsecret", appSecret);
            headers.set("tr_id", tr_id);

            // 쿼리 파라미터 구성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("AUTH", "")
                    .queryParam("EXCD", "NAS")
                    .queryParam("SYMB", symbol);

            String finalUrl = builder.toUriString();

            log.info("Request URL: {}", finalUrl);
            log.info("Request Headers: {}", headers);

            // 요청 엔티티 생성 (body 제거)
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    finalUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            log.info("response : {}", response);
            log.info("Response Status: {}", response.getStatusCode());
            log.info("Response Body: {}", response.getBody());

            // JSON 파싱
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode output = root.path("output");
                log.info("in try");
                // 데이터 변환
                double base = parseDoubleSafe(output.path("base").asText(), 0.0);
                double last = parseDoubleSafe(output.path("last").asText(), 0.0);
                int sign = parseIntSafe(output.path("sign").asText(), 0); // 0이면 변동 없음
                double diff = parseDoubleSafe(output.path("diff").asText(), 0.0);
                double rate = parseDoubleSafe(output.path("rate").asText().trim(), 0.0); // 공백 제거
                long tvol = parseLongSafe(output.path("tvol").asText(), 0L);
                long tamt = parseLongSafe(output.path("tamt").asText(), 0L);
                log.info("last : {}", last);
                ForeignStock stock = new ForeignStock(base, last, sign, diff, rate, tvol, tamt);

                // DB 저장
                return foreignStockRepository.save(stock);

            } catch (Exception e) {
                throw new RuntimeException("API 응답 파싱 실패", e);
            }

        }

        // 안전한 숫자 변환 메서드
        private int parseIntSafe(String value, int defaultValue) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private double parseDoubleSafe(String value, double defaultValue) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private long parseLongSafe(String value, long defaultValue) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }


    }
