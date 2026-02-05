package ahn.gptbook.aiserver.service;

import ahn.gptbook.aiserver.dto.AiBookRecommendationCallbackRequest;
import ahn.gptbook.aiserver.entity.AiBookRecommendation;
import ahn.gptbook.entity.User;
import ahn.gptbook.aiserver.repository.AiBookRecommendationRepository;
import ahn.gptbook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        // 🔥 1) FK 연결을 위해 User 엔티티를 반드시 조회해야 한다
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );

        // 🔥 2) User 엔티티를 기반으로 Recommendation 엔티티 생성
        AiBookRecommendation entity = new AiBookRecommendation(
                user,                     // ← FK(User)
                dto.bookName(),
                dto.genre1(),
                dto.genre2()
        );

        // 🔥 3) 저장
        repository.save(entity);
    }
}
