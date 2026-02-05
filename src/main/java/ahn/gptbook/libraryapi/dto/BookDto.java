package ahn.gptbook.libraryapi.dto;

import ahn.gptbook.entity.Book;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.jsoup.Jsoup;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDto {

    // ISBN
    @JsonAlias({"recomisbn"})
    private String isbn;

    // 제목
    @JsonAlias({"recomtitle"})
    private String title;

    // 저자
    @JsonAlias({"recomauthor"})
    private String author;

    // 출판사
    @JsonAlias({"recompublisher"})
    private String publisher;

    // 썸네일/파일 경로
    @JsonAlias({"recomfilepath"})
    private String thumbnail;

    // 목차
    @JsonAlias({"recommokcha"})
    private String mokcha;

    // 소개/내용  (주의: XML 태그명이 recomcontens 로 되어 있음!)
    @JsonAlias({"recomcontens"})
    private String contents;

    // 발행연도
    @JsonAlias({"publishYear"})
    private String publishYear;


    // ================== 변환 유틸 ==================
    private static String trim(String s) { return s == null ? null : s.trim(); }

    /** ISBN 정규화: 숫자와 X만 남김 */
    private static String normalizeIsbn(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("[^0-9Xx]", "");
        return s.isBlank() ? null : s.toUpperCase();
    }

    /** "2020년", "2020-01-01" → 2020 */
    private static Integer parseYear(String y) {
        if (y == null) return null;
        String d = y.replaceAll("[^0-9]", "");
        if (d.isEmpty()) return null;
        return Integer.parseInt(d.substring(0, Math.min(4, d.length())));
    }

    /** HTML 제거 */
    private static String cleanHtml(String raw) {
        if (raw == null) return null;
        return Jsoup.parse(raw).text();  // 태그 제거 + &nbsp; 변환
    }

    // ================== Entity 변환 ==================
    public static BookDto fromEntity(Book book) {
        return BookDto.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .thumbnail(book.getThumbnail())
                .contents(book.getContents())
                .mokcha(book.getMokcha())
                .publishYear(book.getPublishYear() == null ? null : String.valueOf(book.getPublishYear()))
                .build();
    }

    /** DTO -> Entity (업서트용 기본 변환) */
    public Book toEntity() {
        return Book.builder()
                .isbn(normalizeIsbn(this.isbn))
                .title(trim(this.title))
                .author(trim(this.author))
                .publisher(trim(this.publisher))
                .thumbnail(trim(this.thumbnail))
                .contents(cleanHtml(this.contents))   // ← HTML 제거 후 저장
                .mokcha(cleanHtml(this.mokcha))       // ← HTML 제거 후 저장
                .publishYear(parseYear(this.publishYear))
                .build();
    }
}
