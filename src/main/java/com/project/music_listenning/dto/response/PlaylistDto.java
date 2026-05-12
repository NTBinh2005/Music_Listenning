package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.PlayList;
import com.project.music_listenning.Entity.Song;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PlaylistDto {
    public record PlaylistResponse(
            UUID id,
            String title,
            String description,
            String coverUrl,
            boolean isPublic,
            int songCount,
            OffsetDateTime createdAt,
            OwnerInfo owner
    ) {
        public record OwnerInfo(UUID id, String username) {}

        public static PlaylistResponse from(PlayList p) {
            return new PlaylistResponse(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getCoverUrl(),
                    p.isPublic(),
                    p.getPlaylistSongs().size(),
                    p.getCreatedAt(),
                    new OwnerInfo(p.getUser().getId(), p.getUser().getUsername())
            );
        }
    }

    // Chi tiết playlist kèm danh sách bài
    public record PlaylistDetailResponse(
            UUID id,
            String title,
            String description,
            String coverUrl,
            boolean isPublic,
            OffsetDateTime createdAt,
            OwnerInfo owner,
            List<SongInPlaylist> songs
    ) {
        public record OwnerInfo(UUID id, String username, String avatarUrl) {}

        public record SongInPlaylist(
                UUID id,
                String title,
                String audioUrl,
                String coverUrl,
                int durationSeconds,
                long playCount,
                boolean isPremium,
                int position,
                ArtistInfo artist
        ) {
            public record ArtistInfo(UUID id, String name) {}

            public static SongInPlaylist from(Song s, int position) {
                return new SongInPlaylist(
                        s.getId(), s.getTitle(), s.getAudioUrl(),
                        s.getCoverUrl(), s.getDurationSeconds(),
                        s.getPlayCount(), s.isPremium(), position,
                        new ArtistInfo(s.getArtist().getId(), s.getArtist().getName())
                );
            }
        }
    }

    // Response đơn giản cho like/unlike
    public record LikeResponse(boolean liked, long totalLikes) {}
}
