package ahn.gptbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프론트로 내려줄 추천 도서 응답 DTO
 * - Book 엔티티 기본 정보 + 추천 메타데이터 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRecommendationResponseDto {

    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String thumbnail;
    private Integer publishYear;

    private Integer rank;     // 추천 순위
    private String reason;    // 추천 이유
    private String tag;       // 추천 태그

    private Long userId;      // 추천 요청 사용자 ID (FK)
}
