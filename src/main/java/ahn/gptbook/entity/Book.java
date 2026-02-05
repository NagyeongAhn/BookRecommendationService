package ahn.gptbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "book")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class) // @CreatedDate/@LastModifiedDate 활성화
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    // ISBN
    @Column(name = "isbn", unique = true, length = 500)
    private String isbn;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "author", columnDefinition = "TEXT")
    private String author;

    @Column(name = "publisher", columnDefinition = "TEXT")
    private String publisher;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "contents", columnDefinition = "LONGTEXT")
    private String contents;

    @Column(name = "mokcha", columnDefinition = "LONGTEXT")
    private String mokcha;

    @Column(name = "publish_year")
    private Integer publishYear;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 선택적 부분 업데이트: null/빈값은 덮어쓰지 않는다 */
    public void updateFrom(Book src) {
        if (src == null) return;
        if (hasText(src.title))        this.title = src.title;
        if (src.author != null)        this.author = src.author;
        if (src.publisher != null)     this.publisher = src.publisher;
        if (src.publishYear != null)   this.publishYear = src.publishYear;
        if (src.thumbnail != null)     this.thumbnail = src.thumbnail;
        if (src.contents != null)      this.contents = src.contents;
        if (src.mokcha != null)        this.mokcha = src.mokcha;
    }

    // 기존 시그니처 유지
    public void updateFrom(String title, String author,
                           String publisher, Integer publishYear, String thumbnail) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.thumbnail = thumbnail;
    }

    public void update(String title, String author, String publisher,
                       String contents, String mokcha, Integer publishYear, String thumbnail) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.contents = contents;
        this.mokcha = mokcha;
        this.publishYear = publishYear;
        this.thumbnail = thumbnail;
    }


    private static boolean hasText(String s) { return s != null && !s.isBlank(); }

    @PrePersist
    public void onCreate() {
        var now = java.time.LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
