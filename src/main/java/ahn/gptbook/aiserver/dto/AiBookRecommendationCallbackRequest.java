package ahn.gptbook.aiserver.dto;

import java.util.List;

//AI에게 뭐 받을건지

public record AiBookRecommendationCallbackRequest(Long userId, String bookName, String genre1, String genre2) {
}
