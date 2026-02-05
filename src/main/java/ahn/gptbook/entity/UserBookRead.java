package ahn.gptbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_book_read",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_read_user_book", columnNames = {"user_id","book_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserBookRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey=@ForeignKey(name="fk_read_user"))
    private User user;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="book_id", nullable=false,
            foreignKey=@ForeignKey(name="fk_read_book"))
    private Book book;

    @Column(name="is_read", nullable=false)
    private Boolean isRead = false;

    @Column(name="rating")
    private Integer rating; // 1~5

    @CreatedDate
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
