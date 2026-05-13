package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.PlayHistory;
import com.project.music_listenning.Entity.Song;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PlayHistoryDto {

    /**
     * Client gửi lên khi kết thúc/dừng nghe bài.
     * Gọi khi: user pause, skip, hoặc bài kết thúc tự nhiên.
     */
    public record RecordPlayRequest(
            @NotNull UUID songId,
            @Min(0)  int  playedSeconds   // số giây đã nghe thực tế
    ) {}

    /** Response cho mỗi bài trong lịch sử */
    public record PlayHistoryResponse(
            UUID            id,
            SongInfo        song,
            int             playedSeconds,
            OffsetDateTime  playedAt
    ) {
        public record SongInfo(
                UUID   id,
                String title,
                String audioUrl,
                String coverUrl,
                int    durationSeconds,
                ArtistInfo artist
        ) {
            public record ArtistInfo(UUID id, String name) {}
        }

        public static PlayHistoryResponse from(PlayHistory ph) {
            Song s = ph.getSong();
            return new PlayHistoryResponse(
                    ph.getId(),
                    new SongInfo(
                            s.getId(), s.getTitle(), s.getAudioUrl(),
                            s.getCoverUrl() != null ? s.getCoverUrl()
                                    : (s.getAlbum() != null ? s.getAlbum().getCoverUrl() : null),
                            s.getDurationSeconds(),
                            new SongInfo.ArtistInfo(s.getArtist().getId(), s.getArtist().getName())
                    ),
                    ph.getPlayedSeconds(),
                    ph.getPlayedAt()
            );
        }
    }
}