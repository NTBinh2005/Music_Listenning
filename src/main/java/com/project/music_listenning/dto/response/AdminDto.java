package com.project.music_listenning.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class AdminDto {

    /** Tổng quan hệ thống — hiện trên dashboard */
    public record DashboardStats(
            long totalUsers,
            long totalSongs,
            long totalArtists,
            long totalAlbums,
            long totalPlays,        // tổng lần nghe toàn hệ thống
            long premiumUsers,
            List<DailyPlay> playsLast7Days   // chart 7 ngày gần nhất
    ) {}

    public record DailyPlay(
            String date,       // "2024-01-15"
            long   count
    ) {}

    /** User management */
    public record UserAdminView(
            UUID           id,
            String         username,
            String         email,
            String         role,
            boolean        isActive,
            String         plan,        // subscription plan
            OffsetDateTime createdAt,
            long           totalPlays
    ) {}

    public record PageResponse<T>(
            List<T> content,
            int     page,
            int     size,
            long    totalElements,
            int     totalPages
    ) {}

    /** Requests */
    public record UpdateUserRoleRequest(String role) {}
    public record ToggleUserActiveRequest(boolean isActive) {}

    public record CreateSongRequest(
            String  title,
            String  audioUrl,
            String  coverUrl,
            UUID    artistId,
            UUID    albumId,
            int     durationSeconds,
            Integer trackNumber,
            boolean isPremium
    ) {}

    public record CreateArtistRequest(
            String  name,
            String  bio,
            String  avatarUrl,
            boolean verified
    ) {}

    public record CreateAlbumRequest(
            String title,
            String coverUrl,
            UUID   artistId,
            String releaseDate,
            String type
    ) {}
}