package com.capstone.web.posts.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
public class PostListRequest {
    private Long boardId;      // category id
    private Long authorId;     // member id
    private Integer page = 1;  // default 1
    private Integer size = 20; // default 20
    private String searchType; // TITLE, CONTENT, AUTHOR
    private String keyword;    // optional
    private String sortBy = "createdAt"; // default

    public int pageIndex() {
        int p = page != null && page > 0 ? page : 1;
        return p - 1; // zero-based for Pageable
    }

    public Sort sort() {
        String by = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        // 최신순 우선: createdAt desc, 그 외는 내림차순 기본
        return Sort.by(Sort.Direction.DESC, by);
    }
}
