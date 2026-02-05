package ahn.gptbook.repository;

import ahn.gptbook.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // BookInfoService에서 벌크 조회에 사용
    List<Book> findAllByIsbnIn(List<String> isbns);
}
