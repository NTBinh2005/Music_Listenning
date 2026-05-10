package com.project.music_listenning.dto.response;

import com.project.music_listenning.Entity.Artist;

import java.util.List;
import java.util.UUID;

public class ArtistDto {
    public record ArtistResponse(
            UUID id,
            String name,
            String bio,
            String avatarUrl,
            boolean verified
    ) {
        public static ArtistResponse from(Artist a) {
            return new ArtistResponse(
                    a.getId(), a.getName(), a.getBio(), a.getAvatarUrl(), a.isVerified()
            );
        }
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
