package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.LikedSong;
import com.project.music_listenning.Entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikedSongRepository extends JpaRepository<LikedSong, LikedSong.LikedSongId> {
    boolean existsByUserIdAndSongId(UUID userId, UUID songId);

    void deleteByUserIdAndSongId(UUID userId, UUID songId);

    // Lấy danh sách bài đã like của user, sắp theo mới nhất
    @Query("SELECT ls.song FROM LikedSong ls WHERE ls.user.id = :userId ORDER BY ls.likedAt DESC")
    Page<Song> findLikedSongsByUserId(@Param("userId") UUID userId, Pageable pageable);

    long countByUserId(UUID userId);
}
