package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.SongDTO.*;
import java.util.List;
import java.util.UUID;

public interface SongService {
    public List<SongResponse> getTopSongs(int limit);
    public PageResponse<SongResponse> getSongsByArtist(UUID artistId, int page, int size);
    public SongStreamResponse getStreamUrl(UUID songId);
    public PageResponse<SongResponse> search(String keyword, int page, int size);
}
