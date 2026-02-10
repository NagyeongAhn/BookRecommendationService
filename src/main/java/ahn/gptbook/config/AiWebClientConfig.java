package ahn.gptbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 AI 서버(장르 추천 등)와 통신할 WebClient를 스프링 빈으로 등록
 */
@Configuration
public class AiWebClientConfig {

    @Bean(name = "genreAiWebClient")
    public WebClient genreAiWebClient(@Value("${ai.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

