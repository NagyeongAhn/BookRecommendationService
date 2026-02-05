package ahn.gptbook.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * AI 서버의 프롬프트 응답 DTO
 * - 프론트로 전달 및 DB 저장을 위해 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptResponseDto {

    private String status;          // 응답 상태 ("ok", "error")
    private String prompt;          // 요청한 프롬프트 텍스트
    private List<ResultDto> results; // 추천 결과 리스트

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultDto {
        private Long userId;                 // 유저 ID
        private Long bookId;                 // 책 ID (문자열 → Long 변환 필요)
        private Map<String, Object> metadata; // 책 메타데이터 (title, author, publisher 등)
        private int rank;                    // 순위 (1부터 시작)
        private String reason;               // 추천 이유
        private String tag;                  // 추천 태그
    }
}
