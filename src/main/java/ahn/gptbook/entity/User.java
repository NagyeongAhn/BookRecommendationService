package ahn.gptbook.entity;

import ahn.gptbook.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    //사용자 이름
    @Column(name = "name", unique = true)
    private String name;

    //이메일
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "genres")
    @Convert(converter = StringListConverter.class)
    private List<String> genres;

    @Column(name="has_genres")
    private boolean hasGenres;

    @PrePersist
    @PreUpdate
    private void updateHasGenres() {
        this.hasGenres = (genres != null && !genres.isEmpty());
    }
}
