package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.PlayHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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

    // Tổng số giây đã nghe
    @Query(value = """
    SELECT COALESCE(SUM(played_seconds), 0)
    FROM play_history
    WHERE user_id = :userId AND played_seconds >= 30
    """, nativeQuery = true)
    long countTotalSeconds(@Param("userId") UUID userId);

    // Top nghệ sĩ hay nghe nhất — trả về Map để linh hoạt
    @Query(value = """
    SELECT a.id, a.name, a.avatar_url, COUNT(ph.id) as play_count
    FROM play_history ph
    JOIN songs s ON ph.song_id = s.id
    JOIN artists a ON s.artist_id = a.id
    WHERE ph.user_id = :userId AND ph.played_seconds >= 30
    GROUP BY a.id, a.name, a.avatar_url
    ORDER BY play_count DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> findTopArtistsByUser(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    // Top bài hay nghe nhất
    @Query(value = """
    SELECT s.id, s.title, s.cover_url, a.name as artist_name, COUNT(ph.id) as play_count
    FROM play_history ph
    JOIN songs s ON ph.song_id = s.id
    JOIN artists a ON s.artist_id = a.id
    WHERE ph.user_id = :userId AND ph.played_seconds >= 30
    GROUP BY s.id, s.title, s.cover_url, a.name
    ORDER BY play_count DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> findTopSongsByUser(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    long countByUserId(UUID userId);

    // Đếm history theo ngày (dùng cho chart 7 ngày)
    @Query(value = """
    SELECT COUNT(*) FROM play_history
    WHERE DATE(played_at AT TIME ZONE 'UTC') = :date::date
    """, nativeQuery = true)
    long countByDate(@Param("date") String date);


}
