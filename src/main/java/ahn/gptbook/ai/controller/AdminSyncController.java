package ahn.gptbook.ai.controller;

import ahn.gptbook.ai.service.AiSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books/upsert")
public class AdminSyncController {  //AI로 도서 데이터 전송

    private final AiSyncService aiSyncService;

    /**
     * DB에 저장된 Book 데이터를 AI 서버로 재업서트(resync)
     * 범위를 지정하지 않으면 전체를 보냄
     * @param batchSize 한 번에 보낼 도서 개수 (기본 100)
     * @param startId   시작 Book ID (옵션)
     * @param endId     끝 Book ID (옵션)
     */
    @PostMapping("/resync")
    public ResponseEntity<?> resyncBooks(
            @RequestParam(defaultValue = "100") int batchSize,
            @RequestParam(required = false) Long startId,
            @RequestParam(required = false) Long endId
    ) {
        var result = aiSyncService.syncRangeToAi(batchSize, startId, endId);
        return ResponseEntity.ok(result);
    }
}
