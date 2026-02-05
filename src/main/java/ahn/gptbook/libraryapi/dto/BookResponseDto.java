package ahn.gptbook.libraryapi.dto;

import ahn.gptbook.libraryapi.dto.BookDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookResponseDto {

    private int totalCount;

    private List<BookDto> items;
}
