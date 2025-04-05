package com.joopro.Joosik_Pro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.domain.ForeignStock;
import com.joopro.Joosik_Pro.repository.DomesticStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomesticStockService {

    private final DomesticStockRepository domesticStockRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apis.data.url}")
    private String ISINUrl;

    @Value("${apis.data.serviceKey}")
    private String firstAppKey;

    @Value("${koreainvest.api.second.url}")
    private String apiUrl;

    @Value("${koreainvest.appkey}")
    private String appKey;

    @Value("${koreainvest.appsecret}")
    private String appSecret;

    @Value("${koreainvest.auth.token}")
    private String authToken;

    @Value("${koreainvest.second.tr_id}")
    private String tr_id;


    public void fetchDomesticStock(String symbol) {

        String isinCd = extracted(symbol);


    }

    private String extracted(String symbol) {
        // 한국어 그대로 넣으면 안돼고 인코딩 해야함
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);

        try {
            // 요청 URI 구성
            URI uri = UriComponentsBuilder.fromHttpUrl(ISINUrl)
                    .queryParam("serviceKey", firstAppKey)
                    .queryParam("numOfRows", "1")
                    .queryParam("pageNo", "1")
                    .queryParam("resultType", "json")
                    .queryParam("itmsNm", encodedSymbol)
                    .build(true)
                    .toUri();

            log.info("Request URI: {}", uri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("Response Status: {}", response.getStatusCode());
            log.info("Response Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = root
                        .path("response")
                        .path("body")
                        .path("items")
                        .path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    JsonNode firstItem = itemsNode.get(0);
                    String isinCd = firstItem.path("isinCd").asText();
                    log.info("isinCd: {}", isinCd);
                    return isinCd;
                }
            }
        } catch (Exception e) {
            log.error("API 호출 또는 파싱 실패", e);
            throw new RuntimeException("API 호출 또는 파싱 실패", e);
        }
        return "fail";
    }




}
