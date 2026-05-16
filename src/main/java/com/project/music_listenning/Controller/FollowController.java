package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.FollowService;
import com.project.music_listenning.dto.response.FollowDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * POST /api/artists/:id/follow — toggle follow/unfollow
     */
    @PostMapping("/api/artists/{id}/follow")
    public ResponseEntity<FollowResponse> toggleFollow(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.toggleFollow(id));
    }

    /**
     * GET /api/artists/:id/follow-status
     * Trả về isFollowed + followerCount — dùng khi load trang ArtistPage
     */
    @GetMapping("/api/artists/{id}/follow-status")
    public ResponseEntity<ArtistWithFollowStatus> getFollowStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getArtistWithFollowStatus(id));
    }

    /**
     * GET /api/following — danh sách nghệ sĩ đang follow
     */
    @GetMapping("/api/following")
    public ResponseEntity<List<ArtistWithFollowStatus>> getFollowing() {
        return ResponseEntity.ok(followService.getFollowingList());
    }
}
