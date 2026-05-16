package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Artist;
import com.project.music_listenning.Entity.FollowedArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowedArtistRepository extends JpaRepository<FollowedArtist, UUID> {

    void deleteByUserIdAndArtistId(UUID userId, UUID artistId);

    // Danh sách nghệ sĩ user đang follow — sắp theo mới nhất
    @Query("""
        SELECT fa.artist FROM FollowedArtist fa
        WHERE fa.user.id = :userId
        ORDER BY fa.followedAt DESC
        """)
    List<Artist> findFollowedArtistsByUserId(@Param("userId") UUID userId);

    // Đếm số người follow của 1 nghệ sĩ
    long countByArtistId(UUID artistId);

    // Kiểm tra user có follow artist không
    boolean existsByUserIdAndArtistId(UUID userId, UUID artistId);
}
