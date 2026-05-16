package com.project.music_listenning.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.SongService;
import com.project.music_listenning.dto.response.SongDTO.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;

    /** Top bài phổ biến (trang chủ) */
    @Override
    public List<SongResponse> getTopSongs(int limit) {
        return songRepository
                .findTopSongs(PageRequest.of(0, limit))
                .stream()
                .map(SongResponse::from)
                .toList();
    }

    /** Danh sách bài của nghệ sĩ */
    @Override
    public PageResponse<SongResponse> getSongsByArtist(UUID artistId, int page, int size) {
        Page<Song> result = songRepository.findByArtistIdAndIsActiveTrue(
                artistId, PageRequest.of(page, size, Sort.by("playCount").descending()));

        return toPageResponse(result.map(SongResponse::from));
    }

    /**
     * Lấy audio URL để stream — điểm quan trọng về bảo mật:
     * audioUrl KHÔNG nằm trong SongResponse thông thường, chỉ trả ở đây
     * sau khi kiểm tra quyền Premium.
     */
    @Override
    public SongStreamResponse getStreamUrl(UUID songId) {
        Song song = songRepository.findById(songId)
                .filter(Song::isActive)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));

        // Kiểm tra premium
        if (song.isPremium()) {
            User currentUser = getCurrentUser();
            // TODO: kiểm tra subscription của user — sẽ implement ở Phase 3
            // if (!subscriptionService.isPremium(currentUser.getId())) throw ...
        }

        return SongStreamResponse.from(song);
    }

    @Override
    public PageResponse<SongResponse> search(String keyword, int page, int size) {
        Page<Song> result = songRepository.searchByTitle(
                keyword, PageRequest.of(page, size));
        return toPageResponse(result.map(SongResponse::from));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        // Nếu principal là String (anonymous) → throw
        throw new IllegalStateException("User chưa đăng nhập");
    }
}
