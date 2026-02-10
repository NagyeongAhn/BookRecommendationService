package ahn.gptbook.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 책 정보 업데이트 요청
 * 기존에 존재하는 도서 레코드를 업데이트할 때 필요한 값 전달
 */
public record BookUpdateRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String publisher,
        String contents,
        String mokcha,
        @NotNull Integer publishYear,
        String thumbnail
) {}