package ahn.gptbook.ai.controller;

import ahn.gptbook.ai.client.AiClient;
import ahn.gptbook.ai.dto.AiPromptRequest;
import ahn.gptbook.dto.BookRecommendationResponseDto;
import ahn.gptbook.dto.PromptRequestDto;
import ahn.gptbook.dto.PromptResponseDto;
import ahn.gptbook.entity.Prompt;
import ahn.gptbook.entity.User;
import ahn.gptbook.repository.BookRecommendationRepository;
import ahn.gptbook.repository.PromptRepository;
import ahn.gptbook.repository.UserRepository;
import ahn.gptbook.service.RecommendationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/prompt")
public class PromptRelayController {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final BookRecommendationRepository recommendationRepository;
    private final AiClient aiClient;
    private final RecommendationService recommendationService;

    /**
     * [신규 프롬프트 작성]
     * 1) prompt 저장
     * 2) (요청의 excludeIds가 있으면 그대로 포함하여) AI 호출
     * 3) 결과로 기존 추천 "덮어쓰기" 저장 후 반환
     */
    @PostMapping(
            value = "/text",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<BookRecommendationResponseDto> saveAndSendPrompt(@RequestBody PromptRequestDto request) {
        // 0) 입력 검증
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("text 값이 비어있습니다.");
        if (request.getUserId() == null) throw new IllegalArgumentException("userId 값이 비어있습니다.");

        // 1) 사용자 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 userId 없음: " + request.getUserId()));

        // 2) 프롬프트 저장
        Prompt savedPrompt = promptRepository.save(
                Prompt.builder()
                        .user(user)
                        .promptText(text)
                        .build()
        );

        // 3) AI 서버 호출 (요청에서 넘어온 excludeIds 사용 가능)
        List<Long> exclude = request.getExcludeIds() != null ? request.getExcludeIds() : List.of();


        AiPromptRequest aiReq = AiPromptRequest.builder()
                .userId(user.getId())
                .text(text)
                .excludeIds(exclude)
                .build();

        PromptResponseDto aiResponse = aiClient.sendPrompt(aiReq);

        // 4) 추천 결과 "덮어쓰기" 저장 + 반환
        return recommendationService.replaceRecommendations(savedPrompt, aiResponse);
    }

    /**
     * [다시 추천]
     * - 현재 프롬프트에서 이미 추천된 도서만 제외하고 AI에 재요청
     * - DB 이력(exclude) + (요청 excludeIds) 합집합으로 제외 처리
     * - 서비스가 기존 목록 뒤에 "최대 3권" 추가 저장 후 반환
     * 요청 바디는 PromptRequestDto 재사용(대개 userId/text는 생략 가능)
     */
    @PostMapping(
            value = "/{promptId}/more",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<BookRecommendationResponseDto> recommendMoreForPrompt(
            @PathVariable Long promptId,
            @RequestBody(required = false) PromptRequestDto request
    ) {
        // 1) 프롬프트 확인
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new EntityNotFoundException("Prompt not found: " + promptId));

        // 2) 제외 목록: DB(현재 프롬프트 이력) + 요청 excludeIds(있으면) 합집합
        Set<Long> excludeSet = new HashSet<>(recommendationRepository.findBookIdListByPromptId(promptId));
        if (request != null && request.getExcludeIds() != null && !request.getExcludeIds().isEmpty()) {
            excludeSet.addAll(request.getExcludeIds());
        }
        List<Long> exclude = new ArrayList<>(excludeSet);

        // 3) AI 호출 (현재 프롬프트 텍스트 사용)
        AiPromptRequest aiReq = AiPromptRequest.builder()
                .userId(prompt.getUser().getId())
                .text(prompt.getPromptText())
                .excludeIds(exclude)
                .build();

        PromptResponseDto aiResponse = aiClient.sendPrompt(aiReq);

        // 4) 서비스: 기존 뒤에 3권 추가 저장 + 반환
        return recommendationService.replaceRecommendations(prompt, aiResponse);
    }
}
