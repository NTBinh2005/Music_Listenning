package com.project.music_listenning.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public class ProfileDto {

    /** Thông tin profile đầy đủ */
    public record ProfileResponse(
            String          id,
            String          username,
            String          email,
            String          avatarUrl,
            String          role,
            OffsetDateTime  createdAt,
            ProfileStats    stats,
            SubscriptionInfo subscription
    ) {}

    /** Thống kê nghe nhạc của user */
    public record ProfileStats(
            long totalPlays,          // tổng số lần nghe (>= 30 giây)
            long totalMinutes,        // tổng số phút đã nghe
            long likedSongs,          // số bài đã like
            long followingArtists,    // số nghệ sĩ đang follow
            long playlists,           // số playlist đã tạo
            List<TopArtistStat> topArtists,   // top 3 nghệ sĩ hay nghe nhất
            List<TopSongStat>   topSongs      // top 5 bài hay nghe nhất
    ) {}

    public record TopArtistStat(
            String id,
            String name,
            String avatarUrl,
            long   playCount
    ) {}

    public record TopSongStat(
            String id,
            String title,
            String coverUrl,
            String artistName,
            long   playCount
    ) {}

    public record SubscriptionInfo(
            String  plan,
            boolean isPremium,
            String  endDate    // null nếu FREE
    ) {}

    /** Request cập nhật profile */
    public record UpdateProfileRequest(
            String username,
            String avatarUrl
    ) {}

    /** Request đổi mật khẩu */
    public record ChangePasswordRequest(
            String currentPassword,
            String newPassword
    ) {}
}