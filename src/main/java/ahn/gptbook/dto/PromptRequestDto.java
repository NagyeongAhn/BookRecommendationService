package ahn.gptbook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PromptRequestDto {
    private Long userId;  // 프론트에서 보낸 사용자 ID
    private String text;  // 프론트에서 보낸 텍스트
    private List<Long> excludeIds; // 이미 추천 받은 도서 ID
}
