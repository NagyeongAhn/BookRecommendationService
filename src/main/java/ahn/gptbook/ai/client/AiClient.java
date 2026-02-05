package ahn.gptbook.ai.client;

import ahn.gptbook.ai.dto.AiPromptRequest;
import ahn.gptbook.ai.dto.BookUpsertDto;
import ahn.gptbook.ai.dto.UpsertRequest;
import ahn.gptbook.ai.dto.UpsertResponse;
import ahn.gptbook.dto.PromptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient; // AiServerConfig에서 Bean으로 등록된 WebClient 주입

    /**
     * 📌 사서추천도서 업서트 (기존 기능)
     */
    public UpsertResponse upsertBooks(UpsertRequest request) {
        return aiWebClient.post()
                .uri("/books/upsert/resync")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("AI server error")
                                .flatMap(msg -> Mono.error(
                                        new RuntimeException("AI HTTP " + resp.statusCode() + " - " + msg)
                                ))
                )
                .bodyToMono(UpsertResponse.class)
                .block();
    }

    /**
     * 📌 도서 업데이트 (기존 기능)
     */
    public UpsertResponse updateBook(BookUpsertDto dto) {
        return aiWebClient.post()
                .uri("/books/upsert/update")
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("AI server error")
                                .flatMap(msg -> Mono.error(
                                        new RuntimeException("AI HTTP " + resp.statusCode() + " - " + msg)))
                )
                .bodyToMono(UpsertResponse.class)
                .block();
    }

    /**
     * ✅ 프롬프트 전달 (프론트 → 백엔드 → AI 서버)
     * - AiPromptRequest에 포함된 excludeBookIds를 그대로 JSON으로 전송
     * - null 안전 처리: excludeBookIds가 null이면 빈 리스트로 교체
     */
    public PromptResponseDto sendPrompt(AiPromptRequest request) {
        Objects.requireNonNull(request, "AiPromptRequest must not be null");
        AiPromptRequest safeReq = AiPromptRequest.builder()
                .userId(request.getUserId())
                .text(request.getText())
                .excludeIds(request.getExcludeIds() != null ? request.getExcludeIds() : Collections.emptyList())
                .build();

        return aiWebClient.post()
                .uri("/prompt/text") // ⬅️ AI 서버의 엔드포인트 (서버 구현과 합의된 경로)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(safeReq)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("AI server error")
                                .flatMap(msg -> Mono.error(
                                        new RuntimeException("AI HTTP " + resp.statusCode() + " - " + msg)
                                ))
                )
                .bodyToMono(PromptResponseDto.class)
                .block();
    }

    /**
     * ✅ 오버로드: 편의 메서드
     * - userId, text, excludeBookIds를 바로 받아서 전송
     */
    public PromptResponseDto sendPrompt(Long userId, String text, List<Long> excludeIds) {
        AiPromptRequest req = AiPromptRequest.builder()
                .userId(userId)
                .text(text)
                .excludeIds(excludeIds != null ? excludeIds : Collections.emptyList())
                .build();
        return sendPrompt(req);
    }
}
