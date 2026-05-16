package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "followed_artists")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FollowedArtist {

    @EmbeddedId
    @Builder.Default
    private FollowedArtistId id = new FollowedArtistId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "followed_at", updatable = false)
    @Builder.Default
    private OffsetDateTime followedAt = OffsetDateTime.now();

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class FollowedArtistId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "artist_id")
        private UUID artistId;
    }
}
