package ahn.gptbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Table(name = "user_book_like")
@NoArgsConstructor
@Getter
@Entity
public class UserBookLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: users.id
    @ManyToOne(fetch = FetchType.LAZY)  // 다대일 관계 (UserBookLike N : 1 User)
    @JoinColumn(name = "user_id", nullable = false) // user_id 컬럼 생성 + FK 설정
    private User user;

    // FK: book.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
