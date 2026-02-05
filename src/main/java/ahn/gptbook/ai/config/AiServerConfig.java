package ahn.gptbook.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiServerConfig {

    @Bean
    public WebClient aiWebClient(
            @Value("${ai.base-url}") String baseUrl
    ) {
        return WebClient.builder()
                // AI 서버 주소
                .baseUrl(baseUrl)
                // 기본 요청 헤더 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // 응답 본문 최대 크기 확장 (16MB)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }
}
