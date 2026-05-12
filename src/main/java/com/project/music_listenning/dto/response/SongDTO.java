package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.Song;
import java.util.UUID;

public class SongDTO {

    /**
     * Response trả về client — chỉ expose những field cần thiết,
     * không để lộ internal fields như audioUrl khi is_premium = true
     */
    public record SongResponse(
            UUID id,
            String title,
            String coverUrl,
            String audioUrl,
            int durationSeconds,
            long playCount,
            boolean isPremium,
            ArtistInfo artist,
            AlbumInfo album
    ) {
        public record ArtistInfo(UUID id, String name, String avatarUrl) {}
        public record AlbumInfo(UUID id, String title, String coverUrl) {}

        /** Map từ entity sang DTO */
        public static SongResponse from(Song song) {
            return new SongResponse(
                    song.getId(),
                    song.getTitle(),
                    song.getCoverUrl() != null ? song.getCoverUrl()
                            : (song.getAlbum() != null ? song.getAlbum().getCoverUrl() : null),
                    song.getAudioUrl(),
                    song.getDurationSeconds(),
                    song.getPlayCount(),
                    song.isPremium(),
                    new ArtistInfo(
                            song.getArtist().getId(),
                            song.getArtist().getName(),
                            song.getArtist().getAvatarUrl()
                    ),
                    song.getAlbum() != null
                            ? new AlbumInfo(
                            song.getAlbum().getId(),
                            song.getAlbum().getTitle(),
                            song.getAlbum().getCoverUrl())
                            : null
            );
        }
    }

    /** Response khi stream — chứa audioUrl, chỉ trả khi user có quyền */
    public record SongStreamResponse(
            UUID id,
            String title,
            String audioUrl,
            int durationSeconds
    ) {
        public static SongStreamResponse from(Song song) {
            return new SongStreamResponse(
                    song.getId(),
                    song.getTitle(),
                    song.getAudioUrl(),
                    song.getDurationSeconds()
            );
        }
    }

    /** Pagination wrapper */
    public record PageResponse<T>(
            java.util.List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
