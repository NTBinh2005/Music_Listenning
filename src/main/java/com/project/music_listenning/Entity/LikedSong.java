package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "liked_songs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LikedSong {

    @EmbeddedId
    @Builder.Default
    private LikedSongId id = new LikedSongId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "liked_at", updatable = false)
    @Builder.Default
    private OffsetDateTime likedAt = OffsetDateTime.now();

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class LikedSongId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "song_id")
        private UUID songId;
    }
}
