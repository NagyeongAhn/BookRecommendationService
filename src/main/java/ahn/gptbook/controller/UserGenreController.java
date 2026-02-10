package ahn.gptbook.controller;

import ahn.gptbook.dto.UserGenresRequest;
import ahn.gptbook.service.UserGenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 유저의 선호 장르 설정을 위한 컨트롤러
 * 장르 선택 여부를 조회하고(HasGenres), 선택한 장르 목록을 저장(SaveGenres)하는 API를 제공
 */
@RestController
@RequestMapping("/users")
public class UserGenreController {

    private final UserGenreService userGenreService;

    public UserGenreController(UserGenreService userGenreService) {
        this.userGenreService = userGenreService;
    }

    // 1. 유저가 장르를 이미 선택한 적이 있는지 확인
    // GET /users/{userId}/has-genres
    @GetMapping("/{userId}/has-genres")
    public ResponseEntity<Map<String, Boolean>> hasGenres(@PathVariable Long userId) {
        boolean hasGenres = userGenreService.hasGenres(userId);
        return ResponseEntity.ok(Map.of("hasGenres", hasGenres));
    }

    // 2. 유저의 선호 장르 저장
    // POST /users/{userId}/genres
    @PostMapping("/{userId}/genres")
    public ResponseEntity<Map<String, String>> saveGenres(
            @PathVariable Long userId,
            @RequestBody UserGenresRequest request
    ) {
        userGenreService.saveGenres(userId, request.genres());
        return ResponseEntity.ok(Map.of("message", "genres saved"));
    }
}
