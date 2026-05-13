package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.PlayHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, UUID> {
    /**
     * Lịch sử nghe của user — sắp theo mới nhất.
     * Dùng để hiện "Recently Played".
     */
    @Query("""
        SELECT ph FROM PlayHistory ph
        JOIN FETCH ph.song s
        JOIN FETCH s.artist
        WHERE ph.user.id = :userId
        ORDER BY ph.playedAt DESC
        """)
    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    /**
     * Distinct songs gần đây — mỗi bài chỉ xuất hiện 1 lần
     * (lấy lần nghe gần nhất). Dùng để hiện "Recently Played" trên homepage.
     */
    @Query(value = """
        SELECT DISTINCT ON (ph.song_id)
            ph.*
        FROM play_history ph
        WHERE ph.user_id = :userId
        ORDER BY ph.song_id, ph.played_at DESC
        """, nativeQuery = true)
    List<PlayHistory> findRecentDistinctSongs(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    /**
     * Xóa toàn bộ lịch sử của user
     */
    @Modifying
    @Query("DELETE FROM PlayHistory ph WHERE ph.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Xóa 1 bài khỏi lịch sử
     */
    @Modifying
    @Query("""
        DELETE FROM PlayHistory ph
        WHERE ph.user.id = :userId AND ph.song.id = :songId
        """)
    void deleteByUserIdAndSongId(
            @Param("userId") UUID userId,
            @Param("songId") UUID songId
    );

    // Thống kê — bài nghe nhiều nhất của user (dùng cho recommendation sau)
    @Query(value = """
        SELECT song_id, COUNT(*) as listen_count
        FROM play_history
        WHERE user_id = :userId AND played_seconds >= 30
        GROUP BY song_id
        ORDER BY listen_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMostPlayedSongIds(
            @Param("userId") UUID userId,
            @Param("limit") int limit
    );
}
