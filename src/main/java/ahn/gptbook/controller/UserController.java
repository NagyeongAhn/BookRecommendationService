package ahn.gptbook.controller;

import ahn.gptbook.dto.UserGenresResponse;
import ahn.gptbook.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 특정 사용자(userId)의 “선호/저장된 장르 목록”을 조회해서 반환하는 API
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;

    @GetMapping("/{userId}/genres")
    public ResponseEntity<UserGenresResponse> getUserGenres(@PathVariable Long userId) {
        UserGenresResponse response = userQueryService.getUserGenres(userId);
        return ResponseEntity.ok(response);
    }
}
