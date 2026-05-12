package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "playlist_songs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlaylistSong {

    // Composite key — dùng @EmbeddedId
    @EmbeddedId
    @Builder.Default
    private PlaylistSongId id = new PlaylistSongId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private PlayList playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;

    @Column(name = "added_at", updatable = false)
    @Builder.Default
    private OffsetDateTime addedAt = OffsetDateTime.now();

    // ── Composite Key class ───────────────────────────────────────────────────
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class PlaylistSongId implements Serializable {
        @Column(name = "playlist_id")
        private UUID playlistId;

        @Column(name = "song_id")
        private UUID songId;
    }
}
