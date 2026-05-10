package com.project.music_listenning.Service.Impl;


import com.project.music_listenning.Repository.ArtistRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.ArtistService;
import com.project.music_listenning.dto.response.SongDTO.*;
import com.project.music_listenning.dto.response.ArtistDto.PageResponse;
import com.project.music_listenning.dto.response.ArtistDto.ArtistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;

    @Override
    public PageResponse<ArtistResponse> getAll(int page, int size) {
        Page<ArtistResponse> result = artistRepository
                .findAll(PageRequest.of(page, size, Sort.by("name")))
                .map(ArtistResponse::from);

        return new PageResponse<>(
                result.getContent(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    @Override
    public ArtistResponse getById(UUID id) {
        return artistRepository.findById(id)
                .map(ArtistResponse::from)
                .orElseThrow(() -> new RuntimeException("Nghệ sĩ không tồn tại"));
    }

    @Override
    public PageResponse<SongResponse> getSongs(UUID artistId, int page, int size) {
        Page<SongResponse> result = songRepository
                .findByArtistIdAndIsActiveTrue(
                        artistId, PageRequest.of(page, size, Sort.by("playCount").descending()))
                .map(SongResponse::from);

        return new PageResponse<>(
                result.getContent(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }
}
