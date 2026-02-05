package ahn.gptbook.service;

import ahn.gptbook.dto.BookInfoDto;
import ahn.gptbook.entity.Book;
import ahn.gptbook.libraryapi.dto.BookDto;
import ahn.gptbook.libraryapi.dto.BookResponseDto;
import ahn.gptbook.libraryapi.service.LibraryApiService;
import ahn.gptbook.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookInfoService {

    private final BookRepository bookRepository;
    private final LibraryApiService libraryApiService;

    // ------------------------------------------------------------
    // ① 기존: 컨트롤러에서 수동으로 받은 BookInfoDto 리스트 업서트
    // ------------------------------------------------------------
    /** 국립중앙도서관 API로부터 받은 BookInfoDto 리스트 업서트(Insert or Update) */
    public ImportResult saveRecommendedBooks(List<BookInfoDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return new ImportResult(0, 0, 0, null);

        // 1) 전처리: ISBN 정규화 + 동일 ISBN 제거
        List<BookInfoDto> cleaned = dtos.stream()
                .filter(Objects::nonNull)
                .map(d -> {
                    // 정규화된 ISBN을 미리 주입(중복제거/조회에 사용)
                    String isbn = normalizeIsbn(d.getIsbn());
                    if (isbn == null || isbn.isBlank()) return null;
                    d.setIsbn(isbn);
                    return d;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                BookInfoDto::getIsbn, // 정규화된 ISBN을 key로
                                d -> d,
                                (a, b) -> a // 중복시 첫 번째 유지
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        if (cleaned.isEmpty()) return new ImportResult(0, 0, 0, null);

        // 2) 기존 책 한 번에 로딩
        List<String> isbns = cleaned.stream().map(BookInfoDto::getIsbn).toList();
        Map<String, Book> existingByIsbn = bookRepository.findAllByIsbnIn(isbns)
                .stream()
                .collect(Collectors.toMap(Book::getIsbn, b -> b));

        // 3) 머지(업서트)
        int inserted = 0, updated = 0;
        List<Book> toSave = new ArrayList<>();

        for (BookInfoDto dto : cleaned) {
            String isbn = dto.getIsbn();
            Book current = existingByIsbn.get(isbn);

            String title = nvl(dto.getTitle());
            String author = nvl(dto.getAuthor());
            String publisher = nvl(dto.getPublisher());
            Integer publishYear = dto.getPublishYear();
            String thumbnail = safe(dto.getThumbnail());

            if (current == null) {
                // insert
                Book created = Book.builder()
                        .isbn(isbn)
                        .title(title)
                        .author(author)
                        .publisher(publisher)
                        .publishYear(publishYear)
                        .thumbnail(thumbnail)
                        .build();
                toSave.add(created);
                inserted++;
            } else {
                // update (null/빈값은 덮어쓰지 않음)
                current.updateFrom(title, author, publisher, publishYear, thumbnail);
                toSave.add(current);
                updated++;
            }
        }

        // 4) 벌크 저장
        if (!toSave.isEmpty()) bookRepository.saveAll(toSave);

        return new ImportResult(inserted, updated, cleaned.size(), null);
    }

    // ------------------------------------------------------------
    // ② 추가: 도서관 API 직접 호출 → BookDto → Book 엔티티 업서트
    // ------------------------------------------------------------
    /** 간편 버전: page/size만 */
    public ImportResult importFromLibraryApi(int page, int size) {
        return importFromLibraryApi(page, size, null, null, null);
    }

    /**
     * 고급 버전: 가이드 필터 포함
     * @param page 1-base
     * @param size 페이지 크기
     * @param startDate YYYYMMDD (예: 20240101) nullable
     * @param endDate   YYYYMMDD (예: 20241231) nullable
     * @param drCode    nullable (분류코드; 실제 값은 API 문서 기준 사용)
     */
    public ImportResult importFromLibraryApi(int page, int size,
                                             Integer startDate, Integer endDate, Integer drCode) {
        BookResponseDto resp = libraryApiService
                .getLibrarianPicks(page, size, startDate, endDate, drCode)
                .block();

        int totalCount = (resp != null) ? resp.getTotalCount() : 0;

        List<BookDto> items = (resp != null && resp.getItems() != null)
                ? resp.getItems() : List.of();
        if (items.isEmpty()) return new ImportResult(0, 0, 0, totalCount);

        // 1) DTO → ISBN 정규화 + 중복 제거
        List<BookDto> cleaned = items.stream()
                .filter(Objects::nonNull)
                .map(d -> {
                    // BookDto → Book (toEntity 내부에서 ISBN 정규화/연도 파싱)
                    Book e = d.toEntity();
                    if (e.getIsbn() == null || e.getIsbn().isBlank()) return null;
                    // BookDto에 정규화된 값을 다시 되돌려 두면 아래 중복 제거에도 활용 가능
                    d.setIsbn(e.getIsbn());
                    return d;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(BookDto::getIsbn, d -> d, (a, b) -> a),
                        m -> new ArrayList<>(m.values())
                ));

        if (cleaned.isEmpty()) return new ImportResult(0, 0, 0, totalCount);

        // 2) 기존 도서 일괄 조회
        List<String> isbns = cleaned.stream().map(BookDto::getIsbn).toList();
        Map<String, Book> existingByIsbn = bookRepository.findAllByIsbnIn(isbns)
                .stream()
                .collect(Collectors.toMap(Book::getIsbn, b -> b));

        // 3) 업서트
        int inserted = 0, updated = 0;
        List<Book> toSave = new ArrayList<>();

        for (BookDto dto : cleaned) {
            Book incoming = dto.toEntity();

            Book current = existingByIsbn.get(incoming.getIsbn());
            if (current == null) {
                toSave.add(incoming);
                inserted++;
            } else {
                current.updateFrom(incoming); // ← 부분 업데이트
                toSave.add(current);
                updated++;
            }
        }

        if (!toSave.isEmpty()) bookRepository.saveAll(toSave);

        return new ImportResult(inserted, updated, cleaned.size(), totalCount);
    }

    /**
     * 벌크 import: limit(총 건수 목표)에 도달할 때까지 page를 증가시키며 수집.
     * - 강제 size=10 제거
     * - 첫 페이지에서 totalCount(있으면)를 받아 **조기 종료** 판단
     */
    public ImportResult bulkImportFromLibraryApi(int limit, int size,
                                                 Integer startDate, Integer endDate, Integer drCode) {
        int page = 1;
        int totalProcessed = 0, totalInserted = 0, totalUpdated = 0;
        Integer apiTotalCount = null;

        while (totalProcessed < limit) {
            ImportResult r = importFromLibraryApi(page, size, startDate, endDate, drCode);

            // 첫 호출 시 totalCount 확보
            if (apiTotalCount == null && r.getTotalCount() != null && r.getTotalCount() > 0) {
                apiTotalCount = r.getTotalCount();
                log.info("[IMPORT-BULK] totalCount from API = {}", apiTotalCount);
            }

            totalProcessed += r.getProcessed();
            totalInserted  += r.getInserted();
            totalUpdated   += r.getUpdated();

            log.info("[IMPORT-BULK] page={}, processed={}, inserted={}, updated={}, cumProcessed={}",
                    page, r.getProcessed(), r.getInserted(), r.getUpdated(), totalProcessed);

            // 더 이상 내려올 항목이 없음
            if (r.getProcessed() == 0) break;

            // API totalCount가 있고, 현재 페이지*size가 totalCount 이상이면 종료
            if (apiTotalCount != null && (page * size) >= apiTotalCount) {
                log.info("[IMPORT-BULK] reached API totalCount: {} (page*size={})", apiTotalCount, page * size);
                break;
            }

            // 사용자가 지정한 limit에 도달
            if (totalProcessed >= limit) break;

            page++;
            // 필요 시 rate-limit 배려
            // try { Thread.sleep(100L); } catch (InterruptedException ignore) {}
        }

        return new ImportResult(totalInserted, totalUpdated, totalProcessed, apiTotalCount);
    }

    // ======================== helpers ========================
    private String normalizeIsbn(String rawIsbn) {
        if (rawIsbn == null) return null;
        String s = rawIsbn.replaceAll("[^0-9Xx]", "").trim();
        return s.isEmpty() ? null : s.toUpperCase();
    }

    private String nvl(String s) { return (s == null) ? "" : s.trim(); }
    private String safe(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    @Getter
    @AllArgsConstructor
    public static class ImportResult {
        private final int inserted;
        private final int updated;
        private final int processed;
        /** API에서 내려준 totalCount(없으면 null) */
        private final Integer totalCount;
    }
}
