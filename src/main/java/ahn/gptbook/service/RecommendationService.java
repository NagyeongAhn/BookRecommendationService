package ahn.gptbook.service;

import ahn.gptbook.dto.BookRecommendationResponseDto;
import ahn.gptbook.dto.PromptResponseDto;
import ahn.gptbook.entity.Book;
import ahn.gptbook.entity.BookRecommendation;
import ahn.gptbook.entity.Prompt;
import ahn.gptbook.repository.BookRecommendationRepository;
import ahn.gptbook.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRecommendationRepository recommendationRepository;
    private final BookRepository bookRepository;

    private static final ObjectMapper OM = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * 한 프롬프트에 대한 기존 추천을 모두 지우고,
     * AI 응답을 기반으로 book_recommendation에 새로 저장한 뒤
     * 프론트로 내려줄 DTO 리스트를 만들어 반환한다.
     */
    @Transactional
    public List<BookRecommendationResponseDto> replaceRecommendations(Prompt prompt, PromptResponseDto response) {
        if (prompt == null) throw new IllegalArgumentException("prompt is null");

        var results = (response == null) ? null : response.getResults();
        if (results == null || results.isEmpty()) {
            deleteByPrompt(prompt.getId());
            log.info("[RECO] promptId={} - AI results empty. Cleared previous recommendations.", prompt.getId());
            return List.of();
        }

        // 0) 기존 추천 제거 (중복 방지)
        deleteByPrompt(prompt.getId());

        // 1) 필요한 책 ID를 일괄 조회하여 N+1 최소화
        List<Long> bookIds = results.stream()
                .map(PromptResponseDto.ResultDto::getBookId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        // 2) 저장 + DTO 변환
        List<BookRecommendationResponseDto> savedList = new ArrayList<>();
        for (PromptResponseDto.ResultDto dto : results) {
            Long bookId = dto.getBookId();
            Book book = bookMap.get(bookId);

            if (book == null) {
                // 없는 책은 스킵하거나 예외 — 여기서는 스킵하고 경고
                log.warn("[RECO] promptId={} - book not found: {}", prompt.getId(), bookId);
                continue;
            }

            BookRecommendation rec = recommendationRepository.save(
                    BookRecommendation.builder()
                            .prompt(prompt)
                            .book(book)
                            .user(prompt.getUser())
                            .rank(dto.getRank())      // Integer ↔ 컬럼 rank_
                            .reason(dto.getReason())
                            .tag(dto.getTag())
                            .build()
            );

            savedList.add(BookRecommendationResponseDto.builder()
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .publisher(book.getPublisher())
                    .thumbnail(book.getThumbnail())
                    .publishYear(book.getPublishYear())
                    .rank(rec.getRank())
                    .reason(rec.getReason())
                    .tag(rec.getTag())
                    .userId(rec.getUser().getId())
                    .build());
        }

        // 3) rank 기준 정렬(선택) + 로그로 실제 응답 확인
        savedList.sort(Comparator.comparing(BookRecommendationResponseDto::getRank, Comparator.nullsLast(Integer::compareTo)));

        try {
            log.info("[RECO-RESPONSE] promptId={} size={}\n{}",
                    prompt.getId(), savedList.size(), OM.writeValueAsString(savedList));
        } catch (Exception e) {
            log.warn("[RECO] failed to log response list", e);
        }

        return savedList;
    }

    /** 특정 프롬프트의 기존 추천 삭제 */
    private void deleteByPrompt(Long promptId) {
        var existing = recommendationRepository.findByPromptId(promptId);
        if (!existing.isEmpty()) {
            recommendationRepository.deleteAllInBatch(existing);
            log.debug("[RECO] deleted {} existing recommendations for promptId={}", existing.size(), promptId);
        }
    }
}
