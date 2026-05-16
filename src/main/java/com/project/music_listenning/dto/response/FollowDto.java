package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.Artist;

import java.util.UUID;

public class FollowDto {

    public record FollowResponse(
            boolean followed,      // true = vừa follow, false = vừa unfollow
            long    totalFollowers
    ) {}

    public record ArtistWithFollowStatus(
            UUID    id,
            String  name,
            String  avatarUrl,
            String  bio,
            boolean verified,
            boolean isFollowed,    // user hiện tại có follow không
            long    followerCount
    ) {
        public static ArtistWithFollowStatus from(
                Artist artist, boolean isFollowed, long followerCount) {
            return new ArtistWithFollowStatus(
                    artist.getId(), artist.getName(),
                    artist.getAvatarUrl(), artist.getBio(),
                    artist.isVerified(), isFollowed, followerCount
            );
        }
    }
}