package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Album;
import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Repository.AlbumRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.AlbumService;
import com.project.music_listenning.dto.response.AlbumDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    @Override
    public AlbumDetailResponse getById(UUID id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album không tồn tại"));

        List<Song> songs = songRepository
                .findByAlbumIdAndIsActiveTrueOrderByTrackNumberAsc(id);

        int totalDuration = songs.stream()
                .mapToInt(Song::getDurationSeconds).sum();

        return new AlbumDetailResponse(
                album.getId(),
                album.getTitle(),
                album.getCoverUrl(),
                album.getReleaseDate(),
                album.getType().name(),
                new AlbumDetailResponse.ArtistInfo(
                        album.getArtist().getId(),
                        album.getArtist().getName(),
                        album.getArtist().getAvatarUrl()
                ),
                songs.stream().map(AlbumDetailResponse.SongInAlbum::from).toList(),
                totalDuration
        );
    }

    @Override
    public List<AlbumResponse> getByArtist(UUID artistId) {
        return albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId)
                .stream().map(AlbumResponse::from).toList();
    }
}
