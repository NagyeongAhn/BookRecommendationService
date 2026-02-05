package ahn.gptbook.dto;

import java.util.List;

public record GenreAiResponse(
        String status,
        List<String> genres,
        List<GenreAiResultItem> results
) { }
