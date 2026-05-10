package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.ArtistDto;
import com.project.music_listenning.dto.response.SongDTO;

import java.util.UUID;

public interface ArtistService {
    ArtistDto.PageResponse<ArtistDto.ArtistResponse> getAll(int page, int size);
    ArtistDto.ArtistResponse getById(UUID id);
    ArtistDto.PageResponse<SongDTO.SongResponse> getSongs(UUID artistId, int page, int size);
}
