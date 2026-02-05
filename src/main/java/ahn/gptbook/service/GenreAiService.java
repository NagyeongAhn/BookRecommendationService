package ahn.gptbook.service;

import ahn.gptbook.GenreRecommendRequest;
import ahn.gptbook.client.AiGenreClient;
import ahn.gptbook.dto.GenreAiResponse;
import ahn.gptbook.dto.GenreAiResultItem;
import ahn.gptbook.entity.Book;
import ahn.gptbook.entity.User;
import ahn.gptbook.repository.BookRepository;
import ahn.gptbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreAiService {

    private final UserRepository userRepository;
    private final AiGenreClient aiGenreClient;
    private final GenreBookRecommendationService genreBookRecommendationService;
    private final BookRepository bookRepository;

    public GenreAiResponse sendGenreRecommendToAi(Long userId, List<Long> excludeIds) {

        // 1) user + genres 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<String> genres = user.getGenres();
        if (genres == null || genres.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no genres");
        }

        // 2) 프론트에서 온 excludeIds만 사용 (DB 조회 제거)
        if (excludeIds == null) {
            excludeIds = List.of();
        }

        // AI 요청 바디
        GenreRecommendRequest request = new GenreRecommendRequest(
                user.getId(),
                genres,
                excludeIds
        );

        System.out.println("[Service] 최종 excludeIds = " + excludeIds);
        System.out.println("[Service] AI 요청 바디 = " + request);

        // AI 서버 호출 → 응답 받기
        GenreAiResponse aiResponse = aiGenreClient.requestGenreRecommendation(request);

        System.out.println("===== 📌 AI RAW RESPONSE RECEIVED =====");
        System.out.println(aiResponse);
        System.out.println("======================================");

        // thumbnail 채우기
        List<Long> bookIds = aiResponse.results().stream()
                .map(GenreAiResultItem::getBookId)
                .toList();

        Map<Long, String> thumbByBookId = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        Book::getThumbnail
                ));

        aiResponse.results().forEach(item -> {
            String thumbnail = thumbByBookId.get(item.getBookId());
            item.setThumbnail(thumbnail);
        });

        // DB 저장 (추천 결과)
        genreBookRecommendationService.saveFromAiResponse(user, aiResponse);

        return aiResponse;
    }
}
