package ahn.gptbook.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptRequest {  // AI에 추천 요청

    /** 사용자 ID (필수) */
    private Long userId;

    /** 프론트 텍스트 / 프롬프트 본문 (필수) */
    private String text;

    /**
     * 제외할 도서 ID 목록 (선택)
     * - 현재 프롬프트에서 이미 추천된 도서들을 서버에서 조회하여 전달
     * - 기본값: 빈 리스트
     */
    @Builder.Default
    private List<Long> excludeIds = Collections.emptyList();

    // === 호환성 유지를 위한 기존 생성자 ===
    public AiPromptRequest(Long userId, String text) {
        this.userId = userId;
        this.text = text;
        this.excludeIds = Collections.emptyList();
    }
}
