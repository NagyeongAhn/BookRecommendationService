package ahn.gptbook.libraryapi.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LibraryApiProperties.class)
public class LibraryApiConfig {

    /**
     * 국립중앙도서관 OpenAPI 호출용 WebClient Bean
     * - 응답 버퍼 상한을 5MB로 확대 (기본 256KB → DataBufferLimitException 방지)
     * - 기본 Accept 헤더 제거: 요청 단에서 명시(현재 Service에서 XML로 지정)
     */
    @Bean
    public WebClient libraryWebClient(WebClient.Builder builder, LibraryApiProperties props) {

        // 1) Netty 클라이언트: 연결/읽기 타임아웃
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()));

        // 2) WebClient 인메모리 버퍼 상한 상향 (기본 256KB → 5MB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5MB
                .build();

        // 3) 빌더 조립
        return builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                // 기본 Accept 지정하지 않음(요청별로 지정). 필요 시 .defaultHeader 추가 가능
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    // 요청 로그
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            System.out.println("[LibraryAPI Request] " + req.method() + " " + req.url());
            return Mono.just(req);
        });
    }

    // 응답 로그
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            System.out.println("[LibraryAPI Response] status=" + res.statusCode());
            return Mono.just(res);
        });
    }
}
