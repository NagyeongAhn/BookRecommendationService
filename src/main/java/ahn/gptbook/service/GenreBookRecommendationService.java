package ahn.gptbook.service;

import ahn.gptbook.dto.GenreAiResponse;
import ahn.gptbook.dto.GenreAiResultItem;
import ahn.gptbook.entity.Book;
import ahn.gptbook.entity.GenreBookRecommendation;
import ahn.gptbook.entity.User;
import ahn.gptbook.repository.BookRepository;
import ahn.gptbook.repository.GenreBookRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreBookRecommendationService {

    private final BookRepository bookRepository;
    private final GenreBookRecommendationRepository genreBookRecommendationRepository;

    public void saveFromAiResponse(User user, GenreAiResponse aiResponse) {
        if (!"ok".equalsIgnoreCase(aiResponse.status())) {
            // 예외 또는 로그
            return;
        }

        for (GenreAiResultItem item : aiResponse.results()) {
            Book book = bookRepository.findById(item.getBookId()).orElse(null);
            if (book == null) {
                // TODO: 필요하면 여기서 Book 새로 만들어 저장
                continue;
            }

            GenreBookRecommendation rec = GenreBookRecommendation.builder()
                    .user(user)
                    .book(book)
                    .rank(item.getRank())
                    .reason(item.getReason())
                    .tag(item.getTag())
                    .build();

            genreBookRecommendationRepository.save(rec);
        }
    }
}
