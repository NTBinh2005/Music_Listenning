package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.FollowDto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface FollowService {
    FollowResponse toggleFollow(UUID artistId);
    ArtistWithFollowStatus getArtistWithFollowStatus(UUID artistId);
    List<ArtistWithFollowStatus> getFollowingList();
}
