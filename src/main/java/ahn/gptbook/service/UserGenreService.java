package ahn.gptbook.service;

import ahn.gptbook.dto.UserGenresRequest;
import ahn.gptbook.entity.User;
import ahn.gptbook.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Transactional
public class UserGenreService {

    private final UserRepository userRepository;

    public UserGenreService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1) 유저가 장르를 한 번이라도 저장한 적 있는지
    @Transactional(readOnly = true)
    public boolean hasGenres(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return user.isHasGenres();   // boolean 필드 getter (롬복 @Getter)
    }

    // 2) 유저의 선호 장르 저장 (최대 3개)
    public void saveGenres(Long userId, List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "genres must not be empty");
        }
        if (genres.size() > 3) {
            throw new ResponseStatusException(BAD_REQUEST, "genres size must be <= 3");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        user.setGenres(genres);   // @PreUpdate 에서 hasGenres 자동 true/false 설정
        // save 안 해도 JPA 영속 상태면 flush 때 자동 업데이트, 그래도 명시해도 됨
        // userRepository.save(user);
    }
}
