package ahn.gptbook.libraryapi.controller;

import ahn.gptbook.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library") // 기존 /api/books 와 충돌 피하려고 분리
public class LibraryImportController {

    private final BookInfoService bookInfoService;

    /**
     * 국립중앙도서관 사서추천도서 → DB 업서트
     * page/size는 1-base, 가이드 필터(startDate/endDate/drCode)는 선택
     * 예) POST /api/library/librarian/import?page=1&size=50&startDate=20240101&endDate=20241231&drCode=1
     */
    @PostMapping("/librarian/import")
    public ResponseEntity<?> importLibrarian(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer startDate, // YYYYMMDD
            @RequestParam(required = false) Integer endDate,   // YYYYMMDD
            @RequestParam(required = false) Integer drCode     // 1/6/5/4
    ) {
        var r = bookInfoService.importFromLibraryApi(page, size, startDate, endDate, drCode);
        return ResponseEntity.ok(Map.of(
                "processed", r.getProcessed(),
                "inserted",  r.getInserted(),
                "updated",   r.getUpdated()
        ));
    }

    // 벌크 import (limit 만큼 페이지를 자동으로 돌림)
    @PostMapping("/librarian/import/bulk")
    public ResponseEntity<?> importLibrarianBulk(
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(defaultValue = "500") int size,
            @RequestParam(required = false) Integer startDate,    // YYYYMMDD
            @RequestParam(required = false) Integer endDate,      // YYYYMMDD
            @RequestParam(name = "drCode", required = false) Integer drCodeCamel,
            @RequestParam(name = "drcode", required = false) Integer drCodeLower
    ) {
        Integer drCode = (drCodeCamel != null) ? drCodeCamel : drCodeLower;

        var r = bookInfoService.bulkImportFromLibraryApi(limit, size, startDate, endDate, drCode);
        return ResponseEntity.ok(Map.of(
                "requested",  limit,
                "processed",  r.getProcessed(),
                "inserted",   r.getInserted(),
                "updated",    r.getUpdated()
        ));
    }
}
