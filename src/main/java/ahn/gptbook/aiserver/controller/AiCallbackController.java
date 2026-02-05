package ahn.gptbook.aiserver.controller;

import ahn.gptbook.aiserver.dto.AiBookRecommendationCallbackRequest;
import ahn.gptbook.aiserver.service.AiBookRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiCallbackController {

    private final AiBookRecommendationService aiBookRecommendationService;

    public AiCallbackController(AiBookRecommendationService aiBookRecommendationService) {
        this.aiBookRecommendationService = aiBookRecommendationService;
    }

    // http://192.168.0.8:8080/book-genre
    @PostMapping("/book-genre")
    public ResponseEntity<Void> receiveBookRecommendation(
            @RequestBody AiBookRecommendationCallbackRequest request
    ) {
        aiBookRecommendationService.saveFromCallback(request);
        return ResponseEntity.ok().build();
    }
}
