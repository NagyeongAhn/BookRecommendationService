package ahn.gptbook.libraryapi.service;

import ahn.gptbook.libraryapi.config.LibraryApiProperties;
import ahn.gptbook.libraryapi.dto.BookResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryApiService {

    private final WebClient libraryWebClient;  // LibraryApiConfig에서 등록
    private final LibraryApiProperties props;  // key, baseUrl 등

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final XmlMapper XML = new XmlMapper();

    // ======= 페이징 변환 유틸 (page,size -> startRow/endRow) =======
    private static int startRow(int page, int size) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        return (p - 1) * s + 1;
    }
    private static int endRow(int page, int size) {
        return startRow(page, size) + Math.max(1, size) - 1;
    }

    // ===================== API 호출 =====================

    public Mono<BookResponseDto> getLibrarianPicks(int page, int size) {
        return getLibrarianPicks(page, size, null, null, null);
    }

    public Mono<BookResponseDto> getLibrarianPicks(int page, int size,
                                                   Integer startDate, Integer endDate, Integer drCode) {
        int sr = startRow(page, size);
        int er = endRow(page, size);

        return libraryWebClient.get()
                .uri(uri -> uri.path("/saseoApi.do")
                        .queryParam("key", props.getKey())
                        .queryParam("startRowNumApi", sr)
                        .queryParam("endRowNemApi", er)   // 문서 표기
                        .queryParam("endRowNumApi", er)   // 샘플 표기(둘 다 전송)
                        .queryParamIfPresent("start_date", Optional.ofNullable(startDate))
                        .queryParamIfPresent("end_date",   Optional.ofNullable(endDate))
                        .queryParamIfPresent("drCode",     Optional.ofNullable(drCode))
                        .queryParam("apiType", "xml")
                        .build())
                .exchangeToMono(res ->
                        res.bodyToMono(String.class).defaultIfEmpty("")
                                .map(body -> {
                                    var ct = res.headers().contentType().orElse(null);
                                    if (!res.statusCode().is2xxSuccessful()) {
                                        throw new IllegalStateException("[LibraryAPI] HTTP " + res.statusCode() + " - " + body);
                                    }
                                    String trimmed = body.stripLeading();
                                    boolean looksXml = (ct != null && ct.isCompatibleWith(MediaType.APPLICATION_XML))
                                            || trimmed.startsWith("<");
                                    if (!looksXml) {
                                        log.warn("[LIB] Non-XML response. ct={}, len={}, preview={}",
                                                ct, body.length(), preview(body));
                                        // 비XML이면 빈 결과 반환(페이징 루프가 자연 종료)
                                        return new BookResponseDto(0, java.util.List.of());
                                    }
                                    return parseXmlToBookResponseDto(body);
                                })
                )
                .onErrorResume(e ->
                        Mono.error(new IllegalStateException("[LibraryAPI] fetch/parse failed: " + e.getMessage(), e))
                );
    }

    // 디버깅용 본문 프리뷰
    private static String preview(String s) {
        if (s == null) return "null";
        int n = Math.min(120, s.length());
        return s.substring(0, n).replaceAll("\\s+", " ");
    }


    public Mono<String> getLibrarianPicksAsJson(int page, int size,
                                                Integer startDate, Integer endDate, Integer drCode) {
        int sr = startRow(page, size);
        int er = endRow(page, size);

        return libraryWebClient.get()
                .uri(uri -> uri
                        .path("/saseoApi.do")
                        .queryParam("key", props.getKey())
                        .queryParam("startRowNumApi", sr)
                        .queryParam("endRowNemApi", er)
                        .queryParam("endRowNumApi", er)
                        .queryParamIfPresent("start_date", Optional.ofNullable(startDate))
                        .queryParamIfPresent("end_date",   Optional.ofNullable(endDate))
                        .queryParamIfPresent("drCode",     Optional.ofNullable(drCode))
                        .queryParam("apiType", "xml")
                        .build())
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "[LibraryAPI] HTTP " + res.statusCode() + " - " + body))))
                .bodyToMono(String.class)
                .map(this::xmlToNormalizedJsonString);
    }

    // ====================== XML → JSON 정규화 ======================

    private BookResponseDto parseXmlToBookResponseDto(String xml) {
        try {
            if (xml == null || xml.isBlank()) return new BookResponseDto(0, java.util.List.of());
            JsonNode root = XML.readTree(xml);

            Integer total = findInt(root, "/channel/totalCount", "/totalCount", "/result/totalCount");
            if (total == null) total = 0;

            ArrayNode items = collectItemsFromChannel(root);

            // ✅ 로그 추가
            log.info("[LIB] parsed totalCount={}, items.size={}", total, items.size());

            ObjectNode normalized = JSON.createObjectNode();
            normalized.put("totalCount", total);
            normalized.set("items", items);

            return JSON.treeToValue(normalized, BookResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("[LibraryAPI] XML→DTO 변환 실패: " + e.getMessage(), e);
        }
    }

    private String xmlToNormalizedJsonString(String xml) {
        try {
            JsonNode root = XML.readTree(xml);
            Integer total = findInt(root, "/channel/totalCount", "/totalCount", "/result/totalCount");
            if (total == null) total = 0;

            ArrayNode items = collectItemsFromChannel(root);

            ObjectNode normalized = JSON.createObjectNode();
            normalized.put("totalCount", total);
            normalized.set("items", items);

            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("[LibraryAPI] XML→JSON 문자열 변환 실패: " + e.getMessage(), e);
        }
    }

    // ====================== 유틸리티 ======================

    /** <channel><list><item>...</item></list> 구조 대응 */
    private ArrayNode collectItemsFromChannel(JsonNode root) {
        ArrayNode itemsArr = JSON.createArrayNode();

        JsonNode channel = root.path("channel");
        if (channel.isMissingNode() || channel.isNull()) channel = root;

        JsonNode lists = channel.path("list");
        if (lists.isArray()) {
            for (JsonNode listNode : lists) {
                JsonNode item = listNode.path("item");
                if (item != null && item.isObject() && item.size() > 0) {
                    itemsArr.add(item);
                }
            }
        } else if (lists.isObject()) {
            JsonNode item = lists.path("item");
            if (item != null && item.isObject() && item.size() > 0) {
                itemsArr.add(item);
            }
        }

        // fallback: 혹시 다른 구조일 경우
        if (itemsArr.isEmpty()) {
            for (JsonNode n : channel.findValues("item")) {
                if (n.isObject() && n.size() > 0) {
                    itemsArr.add(n);
                }
            }
        }
        return itemsArr;
    }

    private Integer findInt(JsonNode root, String... paths) {
        for (String p : paths) {
            JsonNode n = root.at(p);
            if (n == null) continue;
            if (n.isNumber()) return n.intValue();
            if (n.isTextual()) {
                try { return Integer.parseInt(n.textValue().replaceAll("[^0-9]", "")); }
                catch (Exception ignore) {}
            }
        }
        return null;
    }
}
