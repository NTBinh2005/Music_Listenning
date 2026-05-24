package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.*;
import com.project.music_listenning.dto.response.ProfileDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PlayHistoryRepository historyRepository;
    private final LikedSongRepository likedSongRepository;
    private final FollowedArtistRepository followRepository;
    private final PlayListRepository playlistRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder          passwordEncoder;

    /** Lấy profile đầy đủ — gộp thông tin user + stats + subscription */
    public ProfileResponse getMyProfile() {
        User user = getCurrentUser();

        ProfileStats    stats = buildStats(user);
        SubscriptionInfo sub  = buildSubscription(user);

        return new ProfileResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                stats,
                sub
        );
    }

    /** Cập nhật username và/hoặc avatarUrl */
    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.username() != null && !request.username().isBlank()) {
            // Kiểm tra username chưa có ai dùng
            if (!request.username().equals(user.getUsername()) &&
                    userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username đã được sử dụng");
            }
            user.setUsername(request.username().trim());
        }

        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl().isBlank() ? null : request.avatarUrl());
        }

        userRepository.save(user);
        return getMyProfile();
    }

    /** Đổi mật khẩu */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // Verify mật khẩu cũ
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (request.newPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ProfileStats buildStats(User user) {
        var userId = user.getId();

        // Tổng số lần nghe và số phút
        long totalPlays   = historyRepository.countByUserId(userId);
        long totalSeconds = historyRepository.countTotalSeconds(userId);
        long totalMins    = totalSeconds / 60;

        // Counts từ các bảng khác
        long liked       = likedSongRepository.countByUserId(userId);
        long following   = followRepository.countByUserId(userId);
        long playlists   = playlistRepository.countByUserId(userId);

        // Top artists (lấy 3)
        List<TopArtistStat> topArtists = historyRepository
                .findTopArtistsByUser(userId, 3)
                .stream()
                .map(row -> new TopArtistStat(
                        row.get("id").toString(),
                        row.get("name").toString(),
                        row.get("avatar_url") != null ? row.get("avatar_url").toString() : null,
                        Long.parseLong(row.get("play_count").toString())
                ))
                .toList();

        // Top songs (lấy 5)
        List<TopSongStat> topSongs = historyRepository
                .findTopSongsByUser(userId, 5)
                .stream()
                .map(row -> new TopSongStat(
                        row.get("id").toString(),
                        row.get("title").toString(),
                        row.get("cover_url") != null ? row.get("cover_url").toString() : null,
                        row.get("artist_name").toString(),
                        Long.parseLong(row.get("play_count").toString())
                ))
                .toList();

        return new ProfileStats(
                totalPlays, totalMins, liked, following, playlists,
                topArtists, topSongs
        );
    }

    private SubscriptionInfo buildSubscription(User user) {
        return subscriptionRepository.findByUserId(user.getId())
                .map(sub -> new SubscriptionInfo(
                        sub.getPlan().name(),
                        sub.isPremiumActive(),
                        sub.getEndDate() != null ? sub.getEndDate().toString() : null
                ))
                .orElse(new SubscriptionInfo("FREE", false, null));
    }

    private User getCurrentUser() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof User u) return u;
        throw new IllegalStateException("Chưa đăng nhập");
    }
}