package com.project.music_listenning.Service;

import com.project.music_listenning.dto.request.PlaylistRequest.*;
import com.project.music_listenning.dto.response.PlaylistDto.*;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PlaylistService {
    List<PlaylistResponse> getMyPlaylists();
    PlaylistDetailResponse getPlaylistDetail(UUID playlistId);
    PlaylistResponse createPlaylist(CreatePlaylistRequest request);
    PlaylistResponse updatePlaylist(UUID playlistId, UpdatePlaylistRequest request);
    void deletePlaylist(UUID playlistId);
    public void addSong(UUID playlistId, UUID songId);
    public void removeSong(UUID playlistId, UUID songId);
    public LikeResponse toggleLike(UUID songId);
    boolean isLiked(UUID songId);
    List<SongResponse> getLikedSongs(int page, int size);

}
