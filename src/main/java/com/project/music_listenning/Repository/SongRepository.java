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
}