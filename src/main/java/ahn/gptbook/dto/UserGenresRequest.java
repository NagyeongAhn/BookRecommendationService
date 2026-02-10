package ahn.gptbook.dto;

import java.util.List;

public record UserGenresRequest(
        Long userId,           // 유저 ID
        List<String> genres,   // 선택한 장르들
        List<Long> excludeIds  // 추천에서 제외할 도서 ID 리스트
) {}
