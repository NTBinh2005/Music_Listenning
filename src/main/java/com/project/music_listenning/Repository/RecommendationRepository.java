package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Song, UUID> {

    /**
     * TẦNG 1 — Collaborative Filtering đơn giản.
     *
     * Logic: Tìm các user khác có cùng sở thích (nghe cùng artist),
     * rồi lấy bài họ nghe nhiều mà user hiện tại chưa nghe.
     *
     * Giải thích query:
     *   - Lấy artist_id user hay nghe (top 5)
     *   - Tìm user khác cũng nghe các artist đó
     *   - Lấy bài các user đó hay nghe mà user hiện tại chưa nghe
     */
    @Query(value = """
        WITH my_artists AS (
            SELECT s.artist_id, COUNT(*) as cnt
            FROM play_history ph
            JOIN songs s ON ph.song_id = s.id
            WHERE ph.user_id = :userId
              AND ph.played_seconds >= 10
            GROUP BY s.artist_id
            ORDER BY cnt DESC
            LIMIT 5
        ),
        similar_users AS (
            SELECT DISTINCT ph.user_id
            FROM play_history ph
            JOIN songs s ON ph.song_id = s.id
            WHERE s.artist_id IN (SELECT artist_id FROM my_artists)
              AND ph.user_id != :userId
            LIMIT 50
        ),
        candidate_songs AS (
            SELECT ph.song_id, COUNT(*) as play_cnt
            FROM play_history ph
            WHERE ph.user_id IN (SELECT user_id FROM similar_users)
              AND ph.played_seconds >= 10
              AND ph.song_id NOT IN (
                  SELECT song_id FROM play_history WHERE user_id = :userId
              )
            GROUP BY ph.song_id
            ORDER BY play_cnt DESC
            LIMIT :limit
        )
        SELECT s.* FROM songs s
        JOIN candidate_songs cs ON s.id = cs.song_id
        WHERE s.is_active = true
        ORDER BY cs.play_cnt DESC
        """, nativeQuery = true)
    List<Song> findCollaborativeRecommendations(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    /**
     * TẦNG 2 — Content-based: cùng artist với bài user hay nghe.
     * Không bao gồm bài đã nghe.
     */
    @Query(value = """
        WITH my_top_artists AS (
            SELECT s.artist_id, COUNT(*) as cnt
            FROM play_history ph
            JOIN songs s ON ph.song_id = s.id
            WHERE ph.user_id = :userId AND ph.played_seconds >= 10
            GROUP BY s.artist_id
            ORDER BY cnt DESC
            LIMIT 3
        )
        SELECT s.* FROM songs s
        WHERE s.artist_id IN (SELECT artist_id FROM my_top_artists)
          AND s.is_active = true
          AND s.id NOT IN (
              SELECT song_id FROM play_history WHERE user_id = :userId
          )
        ORDER BY s.play_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Song> findContentBasedByArtist(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    /**
     * TẦNG 2B — Content-based: cùng genre với bài user hay nghe.
     */
    @Query(value = """
        WITH my_genres AS (
            SELECT sg.genre_id, COUNT(*) as cnt
            FROM play_history ph
            JOIN songs s ON ph.song_id = s.id
            JOIN song_genres sg ON s.id = sg.song_id
            WHERE ph.user_id = :userId AND ph.played_seconds >= 10
            GROUP BY sg.genre_id
            ORDER BY cnt DESC
            LIMIT 3
        )
        SELECT DISTINCT s.* FROM songs s
        JOIN song_genres sg ON s.id = sg.song_id
        WHERE sg.genre_id IN (SELECT genre_id FROM my_genres)
          AND s.is_active = true
          AND s.id NOT IN (
              SELECT song_id FROM play_history WHERE user_id = :userId
          )
        ORDER BY s.play_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Song> findContentBasedByGenre(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    /**
     * TẦNG 3 — Fallback: top songs toàn hệ thống user chưa nghe.
     */
    @Query(value = """
        SELECT s.* FROM songs s
        WHERE s.is_active = true
          AND s.id NOT IN (
              SELECT song_id FROM play_history WHERE user_id = :userId
          )
        ORDER BY s.play_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Song> findTopSongsExcludingPlayed(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    /**
     * "Bởi vì bạn nghe..." — bài tương tự bài user vừa nghe gần nhất.
     */
    @Query(value = """
        WITH last_song AS (
            SELECT s.artist_id, s.id as song_id
            FROM play_history ph
            JOIN songs s ON ph.song_id = s.id
            WHERE ph.user_id = :userId
            ORDER BY ph.played_at DESC
            LIMIT 1
        )
        SELECT s.* FROM songs s
        WHERE s.artist_id = (SELECT artist_id FROM last_song)
          AND s.id != (SELECT song_id FROM last_song)
          AND s.is_active = true
        ORDER BY s.play_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Song> findSimilarToLastPlayed(
            @Param("userId") UUID userId,
            @Param("limit")  int limit
    );

    /** Kiểm tra user có đủ history để recommend không (>= 5 lần nghe) */
    @Query(value = """
        SELECT COUNT(*) FROM play_history
        WHERE user_id = :userId AND played_seconds >= 10
        """, nativeQuery = true)
    long countPlayHistory(@Param("userId") UUID userId);
}
