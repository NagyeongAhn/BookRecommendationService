package ahn.gptbook.controller;

import ahn.gptbook.dto.GenreAiResponse;
import ahn.gptbook.service.AiGenreRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class GenreRecommendController {

    private final AiGenreRecommendationService aiGenreRecommendationService;

    // 프론트: GET /api/recommend/genre?userId=1&excludeIds=10,20,30
    @GetMapping("/genre")
    public ResponseEntity<GenreAiResponse> recommendByGenre(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "excludeIds", required = false) List<Long> excludeIds
    ) {
        log.info("[Controller] 프론트 요청 userId={}, excludeIds={}", userId, excludeIds);

        // 🔥 여기서 1번 + 2번 AI 장르를 모두 합쳐서
        // GenreRecommendRequest(userId, genres, excludeIds)를 만들고
        // AiGenreClient를 통해 1번 AI에 보냄
        GenreAiResponse response = aiGenreRecommendationService.recommendFromFirstAi(userId, excludeIds);

        log.info("[Controller] AI 응답 = {}", response);
        return ResponseEntity.ok(response);
    }
}
