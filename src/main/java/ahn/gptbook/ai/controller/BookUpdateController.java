package ahn.gptbook.ai.controller;

import ahn.gptbook.ai.dto.BookUpdateRequest;
import ahn.gptbook.ai.dto.UpsertResponse;
import ahn.gptbook.ai.service.BookUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books/upsert")
public class BookUpdateController {

    private final BookUpdateService bookUpdateService;

    /**
     * 개별 Book 업데이트 + AI 서버 업서트(update)
     */
    @PutMapping("/update")
    public ResponseEntity<UpsertResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        return ResponseEntity.ok(bookUpdateService.updateAndUpsert(id, request));
    }
}
