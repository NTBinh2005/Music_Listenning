package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

    // Lấy danh sách bài của 1 nghệ sĩ, sắp theo play count
    Page<Song> findByArtistIdAndIsActiveTrue(UUID artistId, Pageable pageable);

    // Lấy tất cả bài trong 1 album, sắp theo track number
    List<Song> findByAlbumIdAndIsActiveTrueOrderByTrackNumberAsc(UUID albumId);

    // Bài phổ biến nhất (top charts)
    @Query("SELECT s FROM Song s WHERE s.isActive = true ORDER BY s.playCount DESC")
    List<Song> findTopSongs(Pageable pageable);

    // Full-text search — dùng PostgreSQL native query
    @Query(value = """
        SELECT * FROM songs
        WHERE is_active = true
          AND to_tsvector('english', title) @@ plainto_tsquery('english', :keyword)
        ORDER BY play_count DESC
        """, nativeQuery = true)
    Page<Song> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra bài đã tồn tại chưa — tránh import trùng


    // Vì Song không có field artistName trực tiếp, dùng JPQL query:
    @Query("SELECT COUNT(s) > 0 FROM Song s WHERE s.title = :title AND s.artist.name = :artistName")
    boolean existsByTitleAndArtistName(@Param("title") String title,
                                       @Param("artistName") String artistName);

    // Search với filter genre và premium
    @Query(value = """
    SELECT s.* FROM songs s
    JOIN artists a ON s.artist_id = a.id
    LEFT JOIN song_genres sg ON s.id = sg.song_id
    LEFT JOIN genres g ON sg.genre_id = g.id
    WHERE s.is_active = true
      AND (
        s.title ILIKE '%' || :keyword || '%'
        OR a.name ILIKE '%' || :keyword || '%'
      )
      AND (:genre IS NULL OR g.slug = :genre)
      AND (:premium IS NULL OR s.is_premium = :premium)
    ORDER BY s.play_count DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Song> searchWithFilter(
            @Param("keyword") String keyword,
            @Param("genre")   String genre,
            @Param("premium") Boolean premium,
            @Param("limit")   int limit,
            @Param("offset")  int offset
    );

    // Count cho pagination
    @Query(value = """
    SELECT COUNT(DISTINCT s.id) FROM songs s
    JOIN artists a ON s.artist_id = a.id
    LEFT JOIN song_genres sg ON s.id = sg.song_id
    LEFT JOIN genres g ON sg.genre_id = g.id
    WHERE s.is_active = true
      AND (s.title ILIKE '%' || :keyword || '%' OR a.name ILIKE '%' || :keyword || '%')
      AND (:genre IS NULL OR g.slug = :genre)
      AND (:premium IS NULL OR s.is_premium = :premium)
    """, nativeQuery = true)
    long countWithFilter(
            @Param("keyword") String keyword,
            @Param("genre")   String genre,
            @Param("premium") Boolean premium
    );

    // Suggestions cho songs (chỉ cần id + title + artist, limit nhỏ)
    @Query("""
    SELECT s FROM Song s
    WHERE s.isActive = true
      AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY s.playCount DESC
    """)
    List<Song> findSuggestionsForSongs(@Param("keyword") String keyword, Pageable pageable);

}