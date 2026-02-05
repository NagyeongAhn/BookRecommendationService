package ahn.gptbook.ai.dto;

public record BookUpsertDto(
        Long id,
        String title,
        String author,
        String publisher,
        String contents,
        String mokcha,
        Integer publishYear
) {}
