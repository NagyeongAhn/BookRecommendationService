package ahn.gptbook.ai.dto;

import java.util.List;

/**
 * upsert 결과 응답
 */
public record UpsertResponse(
        String status,
        Integer upserted,
        List<FailedItem> failed,
        String message
) {
    public record FailedItem(Long id, String message) {}
    public boolean isOk() { return "ok".equalsIgnoreCase(status); }
    public boolean isPartial() { return "partial".equalsIgnoreCase(status); }
}
