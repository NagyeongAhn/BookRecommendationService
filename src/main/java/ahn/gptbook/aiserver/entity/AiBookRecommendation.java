package ahn.gptbook.aiserver.entity;

import ahn.gptbook.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ai_book_recommendation")
public class AiBookRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: user 테이블의 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)  // FK 컬럼 이름
    private User user;   // ← Long userId → User 객체로 바뀜

    @Column(nullable = false, length = 255)
    private String bookName;

    @Column(length = 100)
    private String genre1;

    @Column(length = 100)
    private String genre2;

    protected AiBookRecommendation() {
    }

    public AiBookRecommendation(User user, String bookName, String genre1, String genre2) {
        this.user = user;
        this.bookName = bookName;
        this.genre1 = genre1;
        this.genre2 = genre2;
    }
}
