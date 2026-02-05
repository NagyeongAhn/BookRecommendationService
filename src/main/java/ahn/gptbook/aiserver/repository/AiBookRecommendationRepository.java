package ahn.gptbook.aiserver.repository;

import ahn.gptbook.aiserver.entity.AiBookRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiBookRecommendationRepository extends JpaRepository<AiBookRecommendation, Long> {

    // 기존에 있던 메서드 (유저 전체 레코드 조회)
    List<AiBookRecommendation> findByUserId(Long userId);

    // 1) userId 기준 genre1 DISTINCT 조회
    @Query("select distinct abr.genre1 " +
            "from AiBookRecommendation abr " +
            "where abr.user.id = :userId and abr.genre1 is not null")
    List<String> findDistinctGenre1ByUserId(@Param("userId") Long userId);

    // 2) userId 기준 genre2 DISTINCT 조회
    @Query("select distinct abr.genre2 " +
            "from AiBookRecommendation abr " +
            "where abr.user.id = :userId and abr.genre2 is not null")
    List<String> findDistinctGenre2ByUserId(@Param("userId") Long userId);
}
