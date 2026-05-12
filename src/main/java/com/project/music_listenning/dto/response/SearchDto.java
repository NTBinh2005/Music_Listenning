package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.Album;
import com.project.music_listenning.Entity.Artist;
import com.project.music_listenning.Entity.Song;

import java.util.List;
import java.util.UUID;

public class SearchDto {

    /**
     * Response tổng hợp — trả về songs + artists + albums cùng lúc.
     * Frontend dùng để hiện kết quả theo từng section.
     */
    public record SearchResponse(
            List<SongResult>   songs,
            List<ArtistResult> artists,
            List<AlbumResult>  albums,
            String             keyword,
            int                totalResults
    ) {}

    /**
     * Suggestions — gợi ý khi user đang gõ (chỉ cần title/name, nhẹ hơn)
     * Trả về tối đa 5-8 kết quả, ưu tiên tốc độ
     */
    public record SuggestionsResponse(
            List<Suggestion> suggestions
    ) {}

    public record Suggestion(
            String type,    // "song" | "artist" | "album"
            UUID   id,
            String label,   // tên hiển thị
            String sublabel // artist name hoặc album name
    ) {}

    // ── Result records ────────────────────────────────────────────────────────

    public record SongResult(
            UUID   id,
            String title,
            String audioUrl,
            String coverUrl,
            int    durationSeconds,
            long   playCount,
            boolean isPremium,
            ArtistInfo artist
    ) {
        public record ArtistInfo(UUID id, String name) {}

        public static SongResult from(Song s) {
            return new SongResult(
                    s.getId(), s.getTitle(), s.getAudioUrl(),
                    s.getCoverUrl() != null ? s.getCoverUrl()
                            : (s.getAlbum() != null ? s.getAlbum().getCoverUrl() : null),
                    s.getDurationSeconds(), s.getPlayCount(), s.isPremium(),
                    new ArtistInfo(s.getArtist().getId(), s.getArtist().getName())
            );
        }
    }

    public record ArtistResult(
            UUID    id,
            String  name,
            String  avatarUrl,
            boolean verified
    ) {
        public static ArtistResult from(Artist a) {
            return new ArtistResult(a.getId(), a.getName(), a.getAvatarUrl(), a.isVerified());
        }
    }

    public record AlbumResult(
            UUID   id,
            String title,
            String coverUrl,
            String artistName
    ) {
        public static AlbumResult from(Album a) {
            return new AlbumResult(
                    a.getId(), a.getTitle(), a.getCoverUrl(), a.getArtist().getName());
        }
    }

    // ── Filter params ─────────────────────────────────────────────────────────

    public record SearchFilter(
            String type,    // "all" | "song" | "artist" | "album"
            String genre,   // slug của genre, null = tất cả
            Boolean premium // null = tất cả, true = chỉ premium, false = chỉ free
    ) {}
}
