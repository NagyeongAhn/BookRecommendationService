package ahn.gptbook.ai.dto;
/**
 * 도서 upsert 에 쓰는 도서 1권 단위
 */
public record BookUpsertDto(
        Long id,
        String title,
        String author,
        String publisher,
        String contents,
        String mokcha,
        Integer publishYear
) {}
