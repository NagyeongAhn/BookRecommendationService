package ahn.gptbook.controller;

import ahn.gptbook.dto.UserGenresResponse;
import ahn.gptbook.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;

    // 이미 있던 has-genres랑 같이 두고 써도 됨
    @GetMapping("/{userId}/genres")
    public ResponseEntity<UserGenresResponse> getUserGenres(@PathVariable Long userId) {
        UserGenresResponse response = userQueryService.getUserGenres(userId);
        return ResponseEntity.ok(response);
    }
}
