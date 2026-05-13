package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "play_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /**
     * Số giây thực sự đã nghe.
     * Trigger trong DB sẽ tự tăng play_count của song
     * nếu played_seconds >= 30.
     */
    @Column(name = "played_seconds", nullable = false)
    @Builder.Default
    private int playedSeconds = 0;

    @Column(name = "played_at", updatable = false)
    @Builder.Default
    private OffsetDateTime playedAt = OffsetDateTime.now();
}