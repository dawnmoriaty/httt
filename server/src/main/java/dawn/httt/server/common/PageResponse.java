package dawn.httt.server.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private PageableInfo pageable;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageable(new PageableInfo(page.getNumber(), page.getSize()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class PageableInfo {

        private int pageNumber;
        private int pageSize;
    }
}
