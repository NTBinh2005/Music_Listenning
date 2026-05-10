package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.Album;
import com.project.music_listenning.Entity.Song;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AlbumDto {

    public record AlbumResponse(
            UUID id,
            String title,
            String coverUrl,
            LocalDate releaseDate,
            String type,
            ArtistInfo artist
    ) {
        public record ArtistInfo(UUID id, String name) {}

        public static AlbumResponse from(Album a) {
            return new AlbumResponse(
                    a.getId(), a.getTitle(), a.getCoverUrl(), a.getReleaseDate(),
                    a.getType().name(),
                    new ArtistInfo(a.getArtist().getId(), a.getArtist().getName())
            );
        }
    }

    // Album detail kèm danh sách bài hát
    public record AlbumDetailResponse(
            UUID id,
            String title,
            String coverUrl,
            LocalDate releaseDate,
            String type,
            ArtistInfo artist,
            List<SongInAlbum> songs,
            int totalDurationSeconds
    ) {
        public record ArtistInfo(UUID id, String name, String avatarUrl) {}
        public record SongInAlbum(
                UUID id, String title, String audioUrl,
                int durationSeconds, long playCount,
                boolean isPremium, Integer trackNumber
        ) {
            public static SongInAlbum from(Song s) {
                return new SongInAlbum(
                        s.getId(), s.getTitle(), s.getAudioUrl(),
                        s.getDurationSeconds(), s.getPlayCount(),
                        s.isPremium(), s.getTrackNumber()
                );
            }
        }
    }
}