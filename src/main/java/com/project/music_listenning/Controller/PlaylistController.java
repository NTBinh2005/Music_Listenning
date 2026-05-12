package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.PlaylistService;
import com.project.music_listenning.dto.request.PlaylistRequest.*;
import com.project.music_listenning.dto.response.PlaylistDto.*;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // ── Playlists ─────────────────────────────────────────────────────────────

    // GET /api/playlists/me — playlist của tôi
    @GetMapping("/api/playlists/me")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists() {
        return ResponseEntity.ok(playlistService.getMyPlaylists());
    }

    // GET /api/playlists/:id — chi tiết playlist
    @GetMapping("/api/playlists/{id}")
    public ResponseEntity<PlaylistDetailResponse> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(playlistService.getPlaylistDetail(id));
    }

    // POST /api/playlists — tạo playlist mới
    @PostMapping("/api/playlists")
    public ResponseEntity<PlaylistResponse> create(
            @Valid @RequestBody CreatePlaylistRequest request) {
        return ResponseEntity.ok(playlistService.createPlaylist(request));
    }

    // PUT /api/playlists/:id — sửa playlist
    @PutMapping("/api/playlists/{id}")
    public ResponseEntity<PlaylistResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlaylistRequest request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(id, request));
    }

    // DELETE /api/playlists/:id — xóa playlist
    @DeleteMapping("/api/playlists/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    // ── Playlist Songs ────────────────────────────────────────────────────────

    // POST /api/playlists/:id/songs — thêm bài vào playlist
    @PostMapping("/api/playlists/{id}/songs")
    public ResponseEntity<Void> addSong(
            @PathVariable UUID id,
            @RequestBody AddSongRequest request) {
        playlistService.addSong(id, request.songId());
        return ResponseEntity.ok().build();
    }

    // DELETE /api/playlists/:id/songs/:songId — xóa bài khỏi playlist
    @DeleteMapping("/api/playlists/{id}/songs/{songId}")
    public ResponseEntity<Void> removeSong(
            @PathVariable UUID id,
            @PathVariable UUID songId) {
        playlistService.removeSong(id, songId);
        return ResponseEntity.noContent().build();
    }

    // ── Like / Unlike ─────────────────────────────────────────────────────────

    // POST /api/songs/:id/like — toggle like
    @PostMapping("/api/songs/{id}/like")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable UUID id) {
        return ResponseEntity.ok(playlistService.toggleLike(id));
    }

    // GET /api/songs/:id/liked — kiểm tra đã like chưa
    @GetMapping("/api/songs/{id}/liked")
    public ResponseEntity<Boolean> isLiked(@PathVariable UUID id) {
        return ResponseEntity.ok(playlistService.isLiked(id));
    }

    // GET /api/songs/liked — danh sách bài đã like
    @GetMapping("/api/songs/liked")
    public ResponseEntity<List<SongResponse>> getLikedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(playlistService.getLikedSongs(page, size));
    }
}