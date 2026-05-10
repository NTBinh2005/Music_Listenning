package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.AlbumService;
import com.project.music_listenning.dto.response.AlbumDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // GET /api/albums/:id — chi tiết album kèm danh sách bài
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(albumService.getById(id));
    }

    // GET /api/albums?artistId=xxx — lấy tất cả album của nghệ sĩ
    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getByArtist(
            @RequestParam UUID artistId) {
        return ResponseEntity.ok(albumService.getByArtist(artistId));
    }
}