package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSong.PlaylistSongId> {
    // Đếm số bài trong playlist — để tính position cho bài mới
    int countByPlaylistId(UUID playlistId);

    // Xóa bài khỏi playlist
    void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    // Kiểm tra bài đã trong playlist chưa
    boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    // Cập nhật lại position sau khi xóa bài
    @Modifying
    @Query("""
        UPDATE PlaylistSong ps SET ps.position = ps.position - 1
        WHERE ps.playlist.id = :playlistId AND ps.position > :position
        """)
    void decrementPositionsAfter(
            @Param("playlistId") UUID playlistId,
            @Param("position") int position
    );
}
