package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {
    List<Genre> findAllByOrderByNameAsc();
}
