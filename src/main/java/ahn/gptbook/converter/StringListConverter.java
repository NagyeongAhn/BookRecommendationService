package ahn.gptbook.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        // genres가 null 또는 빈 리스트면 DB에는 null 저장
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        // "장르1,장르2,장르3" 형태로 변환
        return String.join(DELIMITER, attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        // DB 컬럼이 null 또는 빈 문자열이면 빈 리스트 반환
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }
        // 문자열을 "," 기준으로 다시 List<String> 형태로 변환
        return Arrays.asList(dbData.split(DELIMITER));
    }
}
