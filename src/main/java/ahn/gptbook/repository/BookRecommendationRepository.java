package ahn.gptbook.repository;

import ahn.gptbook.entity.BookRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRecommendationRepository extends JpaRepository<BookRecommendation, Long> {

    /** 현재 프롬프트에 이미 저장된 추천 레코드 전체 조회 */
    List<BookRecommendation> findByPromptId(Long promptId);

    /** 현재 프롬프트에서 이미 추천된 book_id 목록만 추출 (AI 호출 시 exclude 용) */
    @Query("select br.book.id from BookRecommendation br where br.prompt.id = :promptId")
    List<Long> findBookIdListByPromptId(Long promptId);

    /** 현재 프롬프트에 동일 도서가 이미 존재하는지 여부 (동시성/중복 저장 방지) */
    boolean existsByPrompt_IdAndBook_Id(Long promptId, Long bookId);

    /** 현재 프롬프트의 추천 레코드 일괄 삭제 (교체 저장 시 사용) */
    long deleteByPrompt_Id(Long promptId);
}
