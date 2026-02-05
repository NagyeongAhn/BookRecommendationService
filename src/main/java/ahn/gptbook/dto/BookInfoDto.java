package ahn.gptbook.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookInfoDto {

    private String id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String thumbnail;
    private String contents;
    private String mokcha;
    private Integer publishYear;


}
