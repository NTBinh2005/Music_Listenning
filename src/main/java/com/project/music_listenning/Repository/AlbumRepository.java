package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {
    Optional<Album> findByTitleIgnoreCaseAndArtistId(String title, UUID artistId);
    Optional<Album> findByArtistIdOrderByReleaseDateDesc(UUID artistId);
}
