package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.ArtistService;
import com.project.music_listenning.dto.response.ArtistDto.*;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    // GET /api/artists?page=0&size=20
    @GetMapping
    public ResponseEntity<PageResponse<ArtistResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(artistService.getAll(page, size));
    }

    // GET /api/artists/:id
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(artistService.getById(id));
    }

    // GET /api/artists/:id/songs?page=0&size=20
    @GetMapping("/{id}/songs")
    public ResponseEntity<PageResponse<SongResponse>> getSongs(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(artistService.getSongs(id, page, size));
    }
}