package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.PlayHistory;
import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.PlayHistoryRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.PlayHistoryService;
import com.project.music_listenning.dto.response.PlayHistoryDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayHistoryServiceImpl implements PlayHistoryService {

    private final PlayHistoryRepository historyRepository;
    private final SongRepository songRepository;

    /**
     * Ghi lịch sử nghe.
     * Được gọi từ frontend khi user pause/skip/kết thúc bài.
     * DB trigger sẽ tự tăng play_count nếu playedSeconds >= 30.
     */
    @Transactional
    @Override
    public void record(RecordPlayRequest request) {
        User currentUser = getCurrentUser();

        // Bỏ qua nếu nghe dưới 5 giây — tránh ghi rác khi user skip nhanh
        if (request.playedSeconds() < 5) {
            log.debug("Bỏ qua record — nghe quá ngắn: {}s", request.playedSeconds());
            return;
        }

        Song song = songRepository.findById(request.songId())
                .orElseThrow(() -> new IllegalArgumentException("Bài hát không tồn tại"));

        PlayHistory history = PlayHistory.builder()
                .user(currentUser)
                .song(song)
                .playedSeconds(request.playedSeconds())
                .build();

        historyRepository.save(history);
        log.debug("Ghi lịch sử: user={}, song={}, seconds={}",
                currentUser.getId(), song.getTitle(), request.playedSeconds());
    }

    /**
     * Lịch sử nghe đầy đủ của user.
     * Mỗi lần nghe là 1 record — có thể nghe 1 bài nhiều lần.
     */
    @Override
    public List<PlayHistoryResponse> getHistory(int limit) {
        UUID userId = getCurrentUser().getId();
        return historyRepository
                .findByUserIdOrderByPlayedAtDesc(
                        userId, PageRequest.of(0, Math.min(limit, 50)))
                .stream()
                .map(PlayHistoryResponse::from)
                .toList();
    }

    /**
     * Recently played — mỗi bài chỉ xuất hiện 1 lần.
     * Dùng ở homepage widget "Nghe gần đây".
     */
    @Override
    public List<PlayHistoryResponse> getRecentlyPlayed(int limit) {
        UUID userId = getCurrentUser().getId();
        return historyRepository
                .findRecentDistinctSongs(userId, PageRequest.of(0, Math.min(limit, 20)))
                .stream()
                .map(PlayHistoryResponse::from)
                .toList();
    }

    /** Xóa toàn bộ lịch sử */
    @Transactional
    @Override
    public void clearAll() {
        UUID userId = getCurrentUser().getId();
        historyRepository.deleteAllByUserId(userId);
    }

    /** Xóa 1 bài khỏi lịch sử */
    @Transactional
    @Override
    public void removeSong(UUID songId) {
        UUID userId = getCurrentUser().getId();
        historyRepository.deleteByUserIdAndSongId(userId, songId);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
