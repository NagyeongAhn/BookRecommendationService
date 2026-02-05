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
@EntityListeners(AuditingEntityListener.class)
@Builder
// 그대로 사용
@Entity
@Table(name="prompt")
public class Prompt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey=@ForeignKey(name="fk_prompt_user"))
    private User user;

    @Lob
    @Column(name="prompt_text", nullable=false)
    private String promptText;

    @CreatedDate
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
}

