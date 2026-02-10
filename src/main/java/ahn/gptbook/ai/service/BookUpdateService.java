package ahn.gptbook.ai.service;

import ahn.gptbook.ai.client.AiClient;
import ahn.gptbook.ai.dto.BookUpsertDto;
import ahn.gptbook.ai.dto.UpsertResponse;
import ahn.gptbook.ai.dto.BookUpdateRequest;
import ahn.gptbook.entity.Book;
import ahn.gptbook.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookUpdateService {

    private final BookRepository bookRepository;
    private final AiClient aiClient;

    @Transactional
    public UpsertResponse updateAndUpsert(Long id, BookUpdateRequest req) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));

        // 1) DB 업데이트
        book.update(
                req.title(),
                req.author(),
                req.publisher(),
                req.contents(),
                req.mokcha(),
                req.publishYear(),
                req.thumbnail()
        );
        bookRepository.flush(); // AI 호출 전 DB 반영

        // 2) AI 서버로 업서트(update)
        BookUpsertDto dto = new BookUpsertDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getContents(),
                book.getMokcha(),
                book.getPublishYear()
        );

        UpsertResponse res = aiClient.updateBook(dto);
        if (res == null || !res.isOk()) {
            throw new RuntimeException("AI upsert(update) failed: " + (res == null ? "null" : res.message()));
        }
        log.info("AI update upsert ok: id={} msg={}", book.getId(), res.message());
        return res;
    }

}
