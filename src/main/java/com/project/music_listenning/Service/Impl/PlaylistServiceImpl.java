package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.*;
import com.project.music_listenning.Repository.LikedSongRepository;
import com.project.music_listenning.Repository.PlayListRepository;
import com.project.music_listenning.Repository.PlaylistSongRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.PlaylistService;
import com.project.music_listenning.dto.request.PlaylistRequest.*;
import com.project.music_listenning.dto.response.PlaylistDto.*;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {
    private final PlayListRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final LikedSongRepository likedSongRepository;
    private final SongRepository songRepository;

    // ── Playlist CRUD ─────────────────────────────────────────────────────────

    /** Lấy tất cả playlist của user đang đăng nhập */
    @Override
    public List<PlaylistResponse> getMyPlaylists() {
        UUID userId = getCurrentUser().getId();
        return playlistRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    /** Chi tiết playlist — bao gồm danh sách bài */
    @Override
    public PlaylistDetailResponse getPlaylistDetail(UUID playlistId) {
        PlayList playlist = findPlaylistOrThrow(playlistId);

        // Kiểm tra quyền xem: public thì ai cũng xem được, private chỉ owner
        if (!playlist.isPublic()) {
            UUID currentUserId = getCurrentUser().getId();
            if (!playlist.getUser().getId().equals(currentUserId)) {
                throw new IllegalArgumentException("Playlist này là private");
            }
        }

        List<PlaylistDetailResponse.SongInPlaylist> songs = playlist.getPlaylistSongs()
                .stream()
                .map(ps -> PlaylistDetailResponse.SongInPlaylist.from(ps.getSong(), ps.getPosition()))
                .toList();

        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getCoverUrl(),
                playlist.isPublic(),
                playlist.getCreatedAt(),
                new PlaylistDetailResponse.OwnerInfo(
                        playlist.getUser().getId(),
                        playlist.getUser().getUsername(),
                        playlist.getUser().getAvatarUrl()
                ),
                songs
        );
    }

    @Transactional
    @Override
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request) {
        User currentUser = getCurrentUser();

        PlayList playlist = PlayList.builder()
                .user(currentUser)
                .title(request.title())
                .description(request.description())
                .isPublic(request.isPublic())
                .build();

        return PlaylistResponse.from(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse updatePlaylist(UUID playlistId, UpdatePlaylistRequest request) {
        PlayList playlist = findOwnedPlaylistOrThrow(playlistId);

        playlist.setTitle(request.title());
        playlist.setDescription(request.description());
        playlist.setPublic(request.isPublic());

        return PlaylistResponse.from(playlistRepository.save(playlist));
    }

    @Transactional
    public void deletePlaylist(UUID playlistId) {
        PlayList playlist = findOwnedPlaylistOrThrow(playlistId);
        playlistRepository.delete(playlist);
    }

    // ── Playlist Songs ────────────────────────────────────────────────────────

    @Transactional
    public void addSong(UUID playlistId, UUID songId) {
        PlayList playlist = findOwnedPlaylistOrThrow(playlistId);

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new IllegalArgumentException("Bài hát đã có trong playlist");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Bài hát không tồn tại"));

        // Position = số bài hiện tại (0-based, thêm vào cuối)
        int position = playlistSongRepository.countByPlaylistId(playlistId);

        PlaylistSong ps = PlaylistSong.builder()
                .id(new PlaylistSong.PlaylistSongId(playlistId, songId))
                .playlist(playlist)
                .song(song)
                .position(position)
                .build();

        playlistSongRepository.save(ps);
    }

    @Transactional
    public void removeSong(UUID playlistId, UUID songId) {
        findOwnedPlaylistOrThrow(playlistId);

        // Lấy position của bài sắp xóa để cập nhật lại các bài sau nó
        playlistSongRepository.findById(new PlaylistSong.PlaylistSongId(playlistId, songId))
                .ifPresent(ps -> {
                    playlistSongRepository.decrementPositionsAfter(playlistId, ps.getPosition());
                    playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
                });
    }

    // ── Like / Unlike ─────────────────────────────────────────────────────────

    @Transactional
    public LikeResponse toggleLike(UUID songId) {
        User currentUser = getCurrentUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Bài hát không tồn tại"));

        boolean alreadyLiked = likedSongRepository
                .existsByUserIdAndSongId(currentUser.getId(), songId);

        if (alreadyLiked) {
            // Unlike
            likedSongRepository.deleteByUserIdAndSongId(currentUser.getId(), songId);
        } else {
            // Like
            LikedSong likedSong = LikedSong.builder()
                    .id(new LikedSong.LikedSongId(currentUser.getId(), songId))
                    .user(currentUser)
                    .song(song)
                    .build();
            likedSongRepository.save(likedSong);
        }

        long totalLikes = likedSongRepository.countByUserId(currentUser.getId());
        return new LikeResponse(!alreadyLiked, totalLikes);
    }

    /** Kiểm tra user đã like bài này chưa — dùng để hiện icon tim */
    public boolean isLiked(UUID songId) {
        UUID userId = getCurrentUser().getId();
        return likedSongRepository.existsByUserIdAndSongId(userId, songId);
    }

    /** Danh sách bài đã like của user */
    public List<SongResponse> getLikedSongs(int page, int size) {
        UUID userId = getCurrentUser().getId();
        return likedSongRepository
                .findLikedSongsByUserId(userId, PageRequest.of(page, size))
                .map(SongResponse::from)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PlayList findPlaylistOrThrow(UUID id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Playlist không tồn tại"));
    }

    private PlayList findOwnedPlaylistOrThrow(UUID id) {
        PlayList playlist = findPlaylistOrThrow(id);
        UUID currentUserId = getCurrentUser().getId();
        if (!playlist.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa playlist này");
        }
        return playlist;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
