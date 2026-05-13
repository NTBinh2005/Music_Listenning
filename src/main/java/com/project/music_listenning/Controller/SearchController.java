package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.SearchService;
import com.project.music_listenning.dto.response.SearchDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /api/search?q=son+tung&type=all&genre=v-pop&premium=false
     *
     * Params:
     *   q       — từ khóa (bắt buộc)
     *   type    — "all" | "song" | "artist" | "album" (default: all)
     *   genre   — slug của genre (optional)
     *   premium — true/false (optional)
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Boolean premium) {

        SearchFilter filter = new SearchFilter(type, genre, premium);
        return ResponseEntity.ok(searchService.search(q, filter));
    }

    /**
     * GET /api/search/suggest?q=son
     * Gợi ý nhanh khi user đang gõ — debounce ở frontend
     */
    @GetMapping("/suggest")
    public ResponseEntity<SuggestionsResponse> suggest(@RequestParam String q) {
        return ResponseEntity.ok(searchService.suggest(q));
    }

    /**
     * GET /api/search/genres
     * Lấy danh sách genres để hiện filter chips
     */
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(searchService.getGenres());
    }
}
