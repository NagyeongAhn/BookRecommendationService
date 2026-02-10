package ahn.gptbook.aiserver.controller;

import ahn.gptbook.aiserver.dto.AiBookRecommendationCallbackRequest;
import ahn.gptbook.aiserver.service.AiBookRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 서버에서 생성한 추천 결과를
 * callback 형태로 받아서 DB에 저장하는 컨트롤러.
 */
@RestController
public class AiCallbackController {

    private final AiBookRecommendationService aiBookRecommendationService;

    public AiCallbackController(AiBookRecommendationService aiBookRecommendationService) {
        this.aiBookRecommendationService = aiBookRecommendationService;
    }

    /**
     * [AI 추천 결과 콜백 수신 API]
     * - AI 시스템이 추천 결과를 만들고 나서 호출하는 엔드포인트
     * - 서버는 받은 데이터를 DB에 저장한 뒤 200 OK를 반환
     * 예) AI가 아래 URL로 POST:
     *   http://192.168.0.8:8080/book-genre
     */
    @PostMapping("/book-genre")
    public ResponseEntity<Void> receiveBookRecommendation(
            @RequestBody AiBookRecommendationCallbackRequest request
    ) {
        aiBookRecommendationService.saveFromCallback(request);
        return ResponseEntity.ok().build();
    }
}
