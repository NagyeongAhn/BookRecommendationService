package ahn.gptbook.service;

import ahn.gptbook.GenreRecommendRequest;
import ahn.gptbook.aiserver.repository.AiBookRecommendationRepository;
import ahn.gptbook.client.AiGenreClient;
import ahn.gptbook.dto.GenreAiResponse;
import ahn.gptbook.dto.GenreAiResultItem;
import ahn.gptbook.entity.Book;
import ahn.gptbook.entity.User;
import ahn.gptbook.repository.BookRepository;
import ahn.gptbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 1번 AI(장르 기반 도서 추천, 음성 인식 기반 추천)에
 * 보낼 장르 리스트를 구성하고, 실제로 호출하는 서비스.
 *
 * - users 테이블의 User.genres (List<String>)
 * - ai_book_recommendation 테이블의 genre1, genre2
 * 를 합쳐서 최종 장르 리스트를 만든 뒤,
 * userId + genres (+ excludeIds) 를 1번 AI에게 보낸다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
// 1번 ai, 2번 ai의 장르 합쳐서 보내는 클래스
public class AiGenreRecommendationService {

    private final UserRepository userRepository;
    private final AiBookRecommendationRepository aiBookRecommendationRepository;
    private final AiGenreClient aiGenreClient;   // 1번 AI 호출용 WebClient
    private final BookRepository bookRepository; // ★ 썸네일 조회용

    /**
     * 1) users.genres
     * 2) ai_book_recommendation.genre1 / genre2
     * 를 합쳐서 최종 장르 리스트를 만들어준다.
     *
     * @param userId 대상 유저 ID
     * @return 1번 AI에게 보낼 장르 리스트 (중복 제거 + 최대 5개)
     */
    public List<String> buildGenresForUser(Long userId) {
        // 1. users 테이블에서 기본 관심 장르 가져오기 (List<String>)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저: " + userId));

        // User.genres 는 이미 List<String> 이라 그대로 사용하면 됨
        List<String> baseGenres = Optional.ofNullable(user.getGenres())
                .orElseGet(List::of);

        // 2. ai_book_recommendation 에서 표정 기반 장르 가져오기
        List<String> genre1List = aiBookRecommendationRepository.findDistinctGenre1ByUserId(userId);
        List<String> genre2List = aiBookRecommendationRepository.findDistinctGenre2ByUserId(userId);

        // 3. 두 쪽 장르 합치고 중복 제거
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(baseGenres);     // users.genres
        merged.addAll(genre1List);     // genre1
        merged.addAll(genre2List);     // genre2

        // 필요하면 상위 N개만 사용 (지금은 5개 기준, 나중에 바꿔도 됨)
        List<String> finalGenres = merged.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .limit(5)
                .toList();

        log.info("[AI-GENRE] userId={} -> baseGenres={}, faceGenres1={}, faceGenres2={}, merged={}",
                userId, baseGenres, genre1List, genre2List, finalGenres);

        return finalGenres;
    }

    /**
     * 최종 장르 리스트 + userId 를 1번 AI에게 보내서
     * 장르 기반 추천을 받는 메서드.
     * - AI가 준 결과(GenreAiResultItem)에 우리 DB의 썸네일/메타데이터를 붙여서 반환한다.
     */
    public GenreAiResponse recommendFromFirstAi(Long userId, List<Long> excludeIds) {

        // 1) 장르 리스트 구성
        List<String> genres = buildGenresForUser(userId);

        // 2) excludeIds null/중복/빈값 방어
        List<Long> safeExcludeIds = Optional.ofNullable(excludeIds)
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        GenreRecommendRequest request = new GenreRecommendRequest(
                userId,
                genres,
                safeExcludeIds
        );

        log.info("[AI-GENRE] Request to 1st AI: userId={}, genres={}, excludeIds={}",
                userId, genres, safeExcludeIds);

        // 3) 1번 AI 호출
        GenreAiResponse response = aiGenreClient.requestGenreRecommendation(request);

        log.info("[AI-GENRE] Raw Response from 1st AI: {}", response);

        // 4) AI 결과에 우리 DB의 썸네일/메타데이터 보강
        List<GenreAiResultItem> fixedResults = new ArrayList<>();

        for (GenreAiResultItem item : response.results()) {

            Long bookId = item.getBookId();
            if (bookId == null) {
                log.warn("[AI-GENRE] GenreAiResultItem에 bookId가 없음: {}", item);
                fixedResults.add(item);
                continue;
            }

            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (optionalBook.isEmpty()) {
                log.warn("[AI-GENRE] Book not found in DB for bookId={}", bookId);
                fixedResults.add(item);
                continue;
            }

            Book book = optionalBook.get();

            // DB 기준 메타데이터로 보강
            item.setTitle(book.getTitle());
            item.setAuthor(book.getAuthor());
            item.setPublisher(book.getPublisher());
            item.setPublishYear(book.getPublishYear());
            item.setThumbnail(book.getThumbnail());

            fixedResults.add(item);
        }

        GenreAiResponse fixedResponse = new GenreAiResponse(
                response.status(),
                response.genres(),
                fixedResults
        );

        log.info("[AI-GENRE] Fixed Response (with thumbnails): {}", fixedResponse);

        return fixedResponse;
    }
}
