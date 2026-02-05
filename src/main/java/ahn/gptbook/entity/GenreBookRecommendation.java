package ahn.gptbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "genre_book_recommendation")
@EntityListeners(AuditingEntityListener.class)
public class GenreBookRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 📌 책 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_rec_book")
    )
    private Book book;


    // 📌 사용자 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_rec_user")
    )
    private User user;

    // 📌 순위 (DB 컬럼명이 rank_라서 매핑)
    @Column(name = "rank_", nullable = false)
    private Integer rank;

    // 📌 추천 이유
    @Column(name = "reason")
    private String reason;

    // 📌 태그
    @Column(name = "tag")
    private String tag;

    // 📌 생성 시각 (JPA Auditing)
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
