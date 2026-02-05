package ahn.gptbook.repository;

import ahn.gptbook.entity.GenreBookRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GenreBookRecommendationRepository
        extends JpaRepository<GenreBookRecommendation, Long> {

    // 유저에게 이미 추천된 책들의 bookId 목록
    @Query("select g.book.id from GenreBookRecommendation g where g.user.id = :userId")
    List<Long> findBookIdsByUserId(@Param("userId") Long userId);
}
