package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Artist;
import com.project.music_listenning.Entity.FollowedArtist;
import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.ArtistRepository;
import com.project.music_listenning.Repository.FollowedArtistRepository;
import com.project.music_listenning.Service.FollowService;
import com.project.music_listenning.dto.response.FollowDto.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowedArtistRepository followRepo;
    private final ArtistRepository artistRepo;

    /**
     * Toggle follow/unfollow — gọi 1 endpoint duy nhất.
     * Trả về trạng thái mới và số follower cập nhật.
     */
    @Transactional
    @Override
    public FollowResponse toggleFollow(UUID artistId) {
        User currentUser = getCurrentUser();
        UUID userId      = currentUser.getId();

        Artist artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Nghệ sĩ không tồn tại"));

        boolean alreadyFollowed = followRepo.existsByUserIdAndArtistId(userId, artistId);

        if (alreadyFollowed) {
            followRepo.deleteByUserIdAndArtistId(userId, artistId);
        } else {
            FollowedArtist follow = FollowedArtist.builder()
                    .id(new FollowedArtist.FollowedArtistId(userId, artistId))
                    .user(currentUser)
                    .artist(artist)
                    .build();
            followRepo.save(follow);
        }

        long totalFollowers = followRepo.countByArtistId(artistId);
        return new FollowResponse(!alreadyFollowed, totalFollowers);
    }

    /** Lấy trạng thái follow + follower count của 1 nghệ sĩ */
    @Override
    public ArtistWithFollowStatus getArtistWithFollowStatus(UUID artistId) {
        Artist artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Nghệ sĩ không tồn tại"));

        UUID    userId      = getCurrentUser().getId();
        boolean isFollowed  = followRepo.existsByUserIdAndArtistId(userId, artistId);
        long    followers   = followRepo.countByArtistId(artistId);

        return ArtistWithFollowStatus.from(artist, isFollowed, followers);
    }

    /** Danh sách nghệ sĩ user đang follow */
    @Override
    public List<ArtistWithFollowStatus> getFollowingList() {
        UUID userId = getCurrentUser().getId();

        return followRepo.findFollowedArtistsByUserId(userId)
                .stream()
                .map(artist -> ArtistWithFollowStatus.from(
                        artist,
                        true,  // đã follow rồi nên luôn true
                        followRepo.countByArtistId(artist.getId())
                ))
                .toList();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        // Nếu principal là String (anonymous) → throw
        throw new IllegalStateException("User chưa đăng nhập");
    }
}
