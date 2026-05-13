package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.PlayHistoryService;
import com.project.music_listenning.dto.response.PlayHistoryDto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryService historyService;

    /**
     * POST /api/history
     * Ghi lịch sử nghe — gọi khi user pause/skip/kết thúc bài.
     */
    @PostMapping
    public ResponseEntity<Void> record(@Valid @RequestBody RecordPlayRequest request) {
        historyService.record(request);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/history?limit=20
     * Lịch sử đầy đủ — mỗi lần nghe là 1 dòng.
     */
    @GetMapping
    public ResponseEntity<List<PlayHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(historyService.getHistory(limit));
    }

    /**
     * GET /api/history/recent?limit=10
     * Recently played — mỗi bài chỉ xuất hiện 1 lần.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PlayHistoryResponse>> getRecentlyPlayed(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(historyService.getRecentlyPlayed(limit));
    }

    /**
     * DELETE /api/history — xóa toàn bộ
     */
    @DeleteMapping
    public ResponseEntity<Void> clearAll() {
        historyService.clearAll();
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/history/:songId — xóa 1 bài
     */
    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> removeSong(@PathVariable UUID songId) {
        historyService.removeSong(songId);
        return ResponseEntity.noContent().build();
    }
}
