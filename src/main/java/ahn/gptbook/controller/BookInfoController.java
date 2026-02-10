package ahn.gptbook.controller;

import ahn.gptbook.dto.BookInfoDto;
import ahn.gptbook.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 추천/수집된 도서 목록을 일괄 입력받아 DB에 저장(신규 삽입/기존 갱신)하고,
 * 처리 결과(총 처리/삽입/갱신 건수)를 반환
 */
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
