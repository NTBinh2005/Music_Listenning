package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Album;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {
    Optional<Album> findByTitleIgnoreCaseAndArtistId(String title, UUID artistId);
    Optional<Album> findByArtistIdOrderByReleaseDateDesc(UUID artistId);
    // Search albums theo title hoặc artist name
    @Query("""
    SELECT a FROM Album a
    WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(a.artist.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY a.releaseDate DESC
    """)
    List<Album> searchByTitleOrArtist(@Param("keyword") String keyword, Pageable pageable);

}
