package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "playlists")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlayList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Quan hệ với PlaylistSong — cascade để xóa playlist thì xóa luôn các bài
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<PlaylistSong> playlistSongs = new ArrayList<>();
}
