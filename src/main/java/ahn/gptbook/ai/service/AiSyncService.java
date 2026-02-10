package ahn.gptbook.ai.service;

import ahn.gptbook.ai.client.AiClient;
import ahn.gptbook.ai.dto.BookUpsertDto;
import ahn.gptbook.ai.dto.UpsertRequest;
import ahn.gptbook.ai.dto.UpsertResponse;
import ahn.gptbook.entity.Book;
import ahn.gptbook.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSyncService {

    private final BookRepository bookRepository; // 로컬 DB 조회용
    private final AiClient aiClient; // 외부 AI 서버와 통신하는 Client

    /**
     * 전체 동기화
     * - DB에 있는 Book 전체를 배치(batchSize) 단위로 끊어서 AI 서버로 전송(upsert)
     */
    @Transactional(readOnly = true)
    public SyncResult syncAllToAi(int batchSize) {
        return syncRangeToAi(batchSize, null, null);
    }

    /**
     * 범위 기반 동기화 (startId ~ endId 사이의 Book만 보냄)
     * - startId ~ endId 사이에 해당하는 Book만 골라서 AI 서버로 전송(upsert)
     */
    @Transactional(readOnly = true)
    public SyncResult syncRangeToAi(int batchSize, Long startId, Long endId) {
        int page = 0, sent = 0, upserted = 0, failed = 0;
        Page<Book> slice;

        do {
            // 1) DB에서 페이지 단위로 읽기 (id 기준 오름차순)
            slice = bookRepository.findAll(
                    PageRequest.of(page++, batchSize, Sort.by("id"))
            );

            // 2) 현재 페이지의 Book 목록에서 startId/endId 범위로 필터링
            List<Book> filtered = slice.getContent().stream()
                    .filter(b -> (startId == null || b.getId() >= startId))
                    .filter(b -> (endId == null || b.getId() <= endId))
                    .toList();

            // 3) 필터링된 Book을 AI 서버로 보낼 DTO 형태로 변환
            List<BookUpsertDto> dtos = filtered.stream()
                    .map(this::mapToDto)
                    .toList();

            if (dtos.isEmpty()) continue;

            // 4) UpsertRequest 생성
            var req = UpsertRequest.upsert(dtos);
            try {
                // 5) AI 서버로 upsert 요청
                UpsertResponse res = aiClient.upsertBooks(req);

                sent += dtos.size();

                // 6) AI 서버 응답 집계
                if (res != null) {
                    upserted += (res.upserted() == null ? 0 : res.upserted());
                    failed += (res.failed() == null ? 0 : res.failed().size());

                    if (res.failed() != null && !res.failed().isEmpty()) {
                        res.failed().forEach(f ->
                                log.warn("AI upsert failed id={} msg={}", f.id(), f.message())
                        );
                    }

                    log.info("AI batch result status={} upserted={} failed={}",
                            res.status(),
                            res.upserted(),
                            (res.failed() == null ? 0 : res.failed().size())
                    );
                }
            } catch (Exception e) {
                failed += dtos.size();
                log.error("AI upsert error on page {}: {}", page - 1, e.getMessage(), e);
            }
        } while (!slice.isLast());

        return new SyncResult(sent, upserted, failed);
    }

    /**
     * Book 엔티티 -> AI 서버 전송용 BookUpsertDto로 매핑
     */
    private BookUpsertDto mapToDto(Book b) {
        return new BookUpsertDto(
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getPublisher(),
                b.getContents(),
                b.getMokcha(),
                b.getPublishYear()
        );
    }

    public record SyncResult(int sent, int upserted, int failed) {}
}
