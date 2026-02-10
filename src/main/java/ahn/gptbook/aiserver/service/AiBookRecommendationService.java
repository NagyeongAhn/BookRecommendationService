package ahn.gptbook.aiserver.service;

import ahn.gptbook.aiserver.dto.AiBookRecommendationCallbackRequest;
import ahn.gptbook.aiserver.entity.AiBookRecommendation;
import ahn.gptbook.entity.User;
import ahn.gptbook.aiserver.repository.AiBookRecommendationRepository;
import ahn.gptbook.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * AI 콜백으로 들어온 추천 결과를 DB에 저장하고,
 * 동시에 해당 사용자의 선호 장르 목록(User.genres)을 누적 갱신하는 서비스
 */
@Service
public class AiBookRecommendationService {

    private final AiBookRecommendationRepository repository;
    private final UserRepository userRepository;

    public AiBookRecommendationService(
            AiBookRecommendationRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    // AI -> 내 서버 콜백 받아서 DB에 저장하는 메서드
    @Transactional
    public void saveFromCallback(AiBookRecommendationCallbackRequest dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        repository.save(new AiBookRecommendation(
                user,
                dto.bookName(),
                dto.genre1(),
                dto.genre2()
        ));

        user.setGenres(mergeGenres(user.getGenres(), dto.genre1(), dto.genre2()));
        userRepository.save(user);
    }


    private List<String> mergeGenres(List<String> current, String g1, String g2) {
        LinkedHashSet<String> set = new LinkedHashSet<>();

        if (current != null) {
            for (String g : current) {
                if (g != null && !g.isBlank()) set.add(g.trim());
            }
        }

        if (g1 != null && !g1.isBlank()) set.add(g1.trim());
        if (g2 != null && !g2.isBlank()) set.add(g2.trim());

        return new ArrayList<>(set);
    }
}
