package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.Impl.AdminService;
import com.project.music_listenning.dto.AdminDto.*;
import com.project.music_listenning.dto.response.AlbumDto.AlbumResponse;
import com.project.music_listenning.dto.response.ArtistDto.ArtistResponse;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // toàn bộ controller chỉ ADMIN
public class AdminController {

    private final AdminService adminService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserAdminView>> getUsers(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "20")  int    size,
            @RequestParam(required = false)     String search) {
        return ResponseEntity.ok(adminService.getUsers(page, size, search));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable UUID id,
            @RequestBody UpdateUserRoleRequest req) {
        adminService.updateUserRole(id, req.role());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/active")
    public ResponseEntity<Void> toggleActive(
            @PathVariable UUID id,
            @RequestBody ToggleUserActiveRequest req) {
        adminService.toggleUserActive(id, req.isActive());
        return ResponseEntity.ok().build();
    }

    // ── Songs ─────────────────────────────────────────────────────────────────

    @GetMapping("/songs")
    public ResponseEntity<PageResponse<SongResponse>> getSongs(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size,
            @RequestParam(required = false)    String search) {
        return ResponseEntity.ok(adminService.getSongs(page, size, search));
    }

    @PostMapping("/songs")
    public ResponseEntity<SongResponse> createSong(@RequestBody CreateSongRequest req) {
        return ResponseEntity.ok(adminService.createSong(req));
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable UUID id) {
        adminService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/songs/{id}/active")
    public ResponseEntity<Void> toggleSongActive(
            @PathVariable UUID id,
            @RequestBody ToggleUserActiveRequest req) {
        adminService.toggleSongActive(id, req.isActive());
        return ResponseEntity.ok().build();
    }

    // ── Artists ───────────────────────────────────────────────────────────────

    @GetMapping("/artists")
    public ResponseEntity<PageResponse<ArtistResponse>> getArtists(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size,
            @RequestParam(required = false)    String search) {
        return ResponseEntity.ok(adminService.getArtists(page, size, search));
    }

    @PostMapping("/artists")
    public ResponseEntity<ArtistResponse> createArtist(@RequestBody CreateArtistRequest req) {
        return ResponseEntity.ok(adminService.createArtist(req));
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable UUID id) {
        adminService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    // ── Albums ────────────────────────────────────────────────────────────────

    @GetMapping("/albums")
    public ResponseEntity<PageResponse<AlbumResponse>> getAlbums(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size,
            @RequestParam(required = false)    String search) {
        return ResponseEntity.ok(adminService.getAlbums(page, size, search));
    }

    @PostMapping("/albums")
    public ResponseEntity<AlbumResponse> createAlbum(@RequestBody CreateAlbumRequest req) {
        return ResponseEntity.ok(adminService.createAlbum(req));
    }

    @DeleteMapping("/albums/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID id) {
        adminService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}
