package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.SongService;
import com.project.music_listenning.dto.response.SongDTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    /** GET /api/songs/top?limit=20 — Trang chủ, public */
    @GetMapping("/top")
    public ResponseEntity<List<SongResponse>> getTopSongs(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(songService.getTopSongs(limit));
    }

    /** GET /api/songs/search?q=lac+troi&page=0&size=20 */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<SongResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(songService.search(q, page, size));
    }

    /**
     * GET /api/songs/{id}/stream — Yêu cầu đăng nhập
     * Trả về audioUrl để frontend dùng Howler.js load
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<SongStreamResponse> getStreamUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(songService.getStreamUrl(id));
    }
}