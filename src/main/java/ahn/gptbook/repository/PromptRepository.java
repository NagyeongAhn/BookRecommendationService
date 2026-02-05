package ahn.gptbook.repository;

import ahn.gptbook.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    // 기본 CRUD 메서드 사용 가능 (save, findById, findAll 등)
}
