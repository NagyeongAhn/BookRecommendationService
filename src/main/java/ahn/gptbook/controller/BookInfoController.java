package ahn.gptbook.controller;

import ahn.gptbook.dto.BookInfoDto;
import ahn.gptbook.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookInfoController {

    private final BookInfoService bookInfoService;

    @PostMapping("/import")
    public ResponseEntity<?> importBooks(@RequestBody List<BookInfoDto> dtos) {
        BookInfoService.ImportResult result = bookInfoService.saveRecommendedBooks(dtos);
        return ResponseEntity.ok(Map.of(
                "processed", result.getProcessed(),
                "inserted", result.getInserted(),
                "updated", result.getUpdated()
        ));
    }
}
