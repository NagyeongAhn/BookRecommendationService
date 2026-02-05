package ahn.gptbook.dto;

import java.util.List;

public record UserGenresResponse(Long userId, List<String> genres) {
}
