package ahn.gptbook.aiserver.dto;

// AI가 추천 결과를 생성한 뒤,
// 서버(콜백 API)로 전달할 요청 바디(request body) DTO.
public record AiBookRecommendationCallbackRequest(Long userId, String bookName, String genre1, String genre2) {
}
