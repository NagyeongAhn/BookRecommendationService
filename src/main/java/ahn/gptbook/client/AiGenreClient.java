package ahn.gptbook.client;

import ahn.gptbook.GenreRecommendRequest;
import ahn.gptbook.dto.GenreAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGenreClient {

    private final WebClient aiWebClient;

    public GenreAiResponse requestGenreRecommendation(GenreRecommendRequest request) {

        try {
            // 🔥 1) AI 서버로 보낼 요청 로그
            log.info("AI request -> /recommend/genre: {}", request);

            // 🔥 2) AI 서버의 RAW JSON 응답 받기
            String rawJson = aiWebClient.post()
                    .uri("/recommend/genre")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 🔥 3) RAW JSON 로그 출력
            log.info("AI raw response: {}", rawJson);

            // 🔥 4) JSON → DTO 변환
            ObjectMapper mapper = new ObjectMapper();
            GenreAiResponse parsed = mapper.readValue(rawJson, GenreAiResponse.class);

            // 🔥 5) 파싱된 DTO 로그
            log.info("AI response parsed: {}", parsed);

            return parsed;

        } catch (Exception e) {
            log.error("AI genre recommendation failed", e);
            throw new RuntimeException("AI genre recommendation failed", e);
        }
    }
}
