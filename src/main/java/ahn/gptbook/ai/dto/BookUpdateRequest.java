package ahn.gptbook.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookUpdateRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String publisher,
        String contents,
        String mokcha,
        @NotNull Integer publishYear,
        String thumbnail
) {}