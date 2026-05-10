package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.AlbumDto;

import java.util.List;
import java.util.UUID;

public interface AlbumService {
    AlbumDto.AlbumDetailResponse getById(UUID id);
    List<AlbumDto.AlbumResponse> getByArtist(UUID artistId);
}
