package ahn.gptbook.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenreAiResultItem {
    Long userId;
    Long bookId;
    String title;
    String author;
    String publisher;
    Integer publishYear;
    Integer rank;
    String reason;
    String tag;
    String thumbnail;
}

