package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findByNameIgnoreCase(String name);
}
