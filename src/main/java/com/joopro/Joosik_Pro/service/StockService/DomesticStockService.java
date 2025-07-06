package com.joopro.Joosik_Pro.service.StockService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.domain.DomesticStock;
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


    public DomesticStock FindDomesticStock(String symbol) {
        String isinCd = extractedISIN(symbol);
        String stockCode = isinCd.substring(3, 9);
        log.info("stockCode : {}", stockCode);
        DomesticStock domesticStock = fetchDomesticStock(stockCode);
        return domesticStock;
    }

    public String extractedISIN(String symbol) {
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


    public DomesticStock fetchDomesticStock(String isin) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", "Bearer " + authToken);
            headers.set("custtype", "P");
            headers.set("appkey", appKey);
            headers.set("appsecret", appSecret);
            headers.set("tr_id", tr_id);

            // 쿼리 파라미터 구성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                    .queryParam("FID_INPUT_ISCD", isin);

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
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode output = root.path("output");

                double 현재가 = output.path("stck_prpr").asDouble();
                double 최고가 = output.path("stck_hgpr").asDouble();
                double 최저가 = output.path("stck_lwpr").asDouble();
                double 상한가 = output.path("stck_mxpr").asDouble();
                double 하한가 = output.path("stck_llam").asDouble();
                double from250HighPrice = output.path("d250_hgpr").asDouble();
                double from250LowPrice = output.path("d250_lwpr").asDouble();
                double fromYearHighPrice = output.path("stck_dryy_hgpr").asDouble();
                double fromYearLowPrice = output.path("stck_dryy_lwpr").asDouble();
                double from52wHighPrice = output.path("w52_hgpr").asDouble();
                double from52wLowPrice = output.path("w52_lwpr").asDouble();
                double per = output.path("per").asDouble();
                double pbr = output.path("pbr").asDouble();
                double eps = output.path("eps").asDouble();

                DomesticStock domesticStock = DomesticStock.builder()
                        .현재가(현재가)
                        .최고가(최고가)
                        .최저가(최저가)
                        .상한가(상한가)
                        .하한가(하한가)
                        .from250HighPrice(from250HighPrice)
                        .from250LowPrice(from250LowPrice)
                        .fromYearHighPrice(fromYearHighPrice)
                        .fromYearLowPrice(fromYearLowPrice)
                        .from52wHighPrice(from52wHighPrice)
                        .from52wLowPrice(from52wLowPrice)
                        .per(per)
                        .pbr(pbr)
                        .eps(eps)
                        .build();

                return domesticStock;
            }

        } catch (Exception e) {
            log.error("국내주식 정보 조회 중 오류", e);
            throw new RuntimeException("국내주식 정보 파싱 실패", e);
        }
        return null;
    }

}
