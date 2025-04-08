    package com.joopro.Joosik_Pro.service;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.joopro.Joosik_Pro.domain.DomesticStock;
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

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = null;
                try {
                    root = objectMapper.readTree(response.getBody());
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
                JsonNode output = root.path("output");

                double 현재가 = output.path("stck_prpr").asDouble();
                double 전일종가 = output.path("stck_hgpr").asDouble();
                double 거래량 = output.path("stck_lwpr").asDouble();

                ForeignStock foreignStock = ForeignStock.builder()
                        .현재가(현재가)
                        .전일종가(전일종가)
                        .거래량(거래량)
                        .build();

                return foreignStock;
            }

            return null;
        }

    }
