package ahn.gptbook.libraryapi.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "library.api")
public class LibraryApiProperties {

    @NotBlank
    private String key;   // 국립중앙도서관 API 인증키

    @NotBlank
    private String baseUrl;   // API 베이스 URL (예: https://nl.go.kr/api)

    @Positive
    private int connectTimeoutMs = 5000;   // 연결 타임아웃 (기본값 5초)

    @Positive
    private int readTimeoutMs = 10000;     // 응답 대기 타임아웃 (기본값 10초)
}
