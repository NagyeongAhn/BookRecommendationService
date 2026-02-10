package ahn.gptbook.ai.dto;

import java.util.List;

/**
 * upsert 실행 요청 (여러 건)
 */
public record UpsertRequest(
        List<BookUpsertDto> books,
        String mode,
        Boolean skip_if_unchanged
) {
    public static UpsertRequest upsert(List<BookUpsertDto> books) {
        return new UpsertRequest(books, "upsert", true);
    }
}
