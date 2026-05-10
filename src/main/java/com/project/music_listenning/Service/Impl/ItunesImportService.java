package com.project.music_listenning.Service.Impl;


import com.project.music_listenning.Entity.Album;
import com.project.music_listenning.Entity.Artist;
import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Repository.AlbumRepository;
import com.project.music_listenning.Repository.ArtistRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.dto.response.ItunesDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItunesImportService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    // RestTemplate là HTTP client có sẵn của Spring — gọi API bên ngoài
    private final RestTemplate restTemplate;

    private static final String ITUNES_API = "https://itunes.apple.com/search";

    /**
     * Tìm kiếm bài hát trên iTunes theo keyword và import vào DB.
     *
     * @param keyword  VD: "son tung", "my tam", "vpop"
     * @param limit    Số bài tối đa (iTunes max = 200)
     * @param country  "VN" để ưu tiên nhạc Việt
     */
    @Transactional
    public ImportResult searchAndImport(String keyword, int limit, String country) {
        // 1. Gọi iTunes API
        List<ItunesTrack> tracks = fetchFromItunes(keyword, limit, country);
        log.info("iTunes trả về {} tracks cho keyword: '{}'", tracks.size(), keyword);

        int imported = 0;
        int skipped  = 0;
        List<String> importedTitles = new ArrayList<>();

        for (ItunesTrack track : tracks) {
            // Bỏ qua nếu không phải song hoặc không có audio preview
            if (!track.isSong() || track.previewUrl() == null) {
                skipped++;
                continue;
            }

            // Bỏ qua nếu bài đã có trong DB (check theo title + artistName)
            if (songRepository.existsByTitleAndArtistName(
                    track.trackName(), track.artistName())) {
                log.debug("Bỏ qua (đã tồn tại): {} - {}", track.artistName(), track.trackName());
                skipped++;
                continue;
            }

            try {
                // 2. Upsert Artist — tạo mới nếu chưa có, lấy existing nếu đã có
                Artist artist = upsertArtist(track.artistName());

                // 3. Upsert Album — tương tự
                Album album = null;
                if (track.collectionName() != null) {
                    album = upsertAlbum(track, artist);
                }

                // 4. Tạo Song
                Song song = Song.builder()
                        .title(track.trackName())
                        .audioUrl(track.previewUrl())       // URL preview 30s từ iTunes
                        .coverUrl(track.artworkUrl500())    // ảnh 500x500
                        .durationSeconds(track.durationSeconds())
                        .trackNumber(track.trackNumber())
                        .artist(artist)
                        .album(album)
                        .playCount(0L)
                        .isPremium(false)
                        .isActive(true)
                        .build();

                songRepository.save(song);
                importedTitles.add(track.artistName() + " - " + track.trackName());
                imported++;

                log.info("Imported: {} - {}", track.artistName(), track.trackName());

            } catch (Exception e) {
                log.error("Lỗi import track: {} - {}: {}",
                        track.artistName(), track.trackName(), e.getMessage());
                skipped++;
            }
        }

        return new ImportResult(tracks.size(), imported, skipped, importedTitles);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<ItunesTrack> fetchFromItunes(String keyword, int limit, String country) {
        String url = UriComponentsBuilder.fromHttpUrl(ITUNES_API)
                .queryParam("term",    keyword)
                .queryParam("country", country)
                .queryParam("media",   "music")
                .queryParam("entity",  "song")
                .queryParam("limit",   Math.min(limit, 200))  // iTunes max = 200
                .build()
                .toUriString();

        log.info("Gọi iTunes API: {}", url);

        SearchResponse response = restTemplate.getForObject(url, SearchResponse.class);

        if (response == null || response.results() == null) return List.of();
        return response.results();
    }

    private Artist upsertArtist(String name) {
        return artistRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Artist newArtist = Artist.builder()
                            .name(name)
                            .verified(false)
                            .build();
                    return artistRepository.save(newArtist);
                });
    }

    private Album upsertAlbum(ItunesTrack track, Artist artist) {
        return albumRepository
                .findByTitleIgnoreCaseAndArtistId(track.collectionName(), artist.getId())
                .orElseGet(() -> {
                    Album newAlbum = Album.builder()
                            .title(track.collectionName())
                            .coverUrl(track.artworkUrl500())
                            .artist(artist)
                            .type(Album.AlbumType.ALBUM)
                            .build();
                    return albumRepository.save(newAlbum);
                });
    }
}
