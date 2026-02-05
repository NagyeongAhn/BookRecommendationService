package ahn.gptbook;

import java.util.List;

public record GenreRecommendRequest(
        Long userId,
        List<String> genres,
        List<Long> excludeIds
) { }
