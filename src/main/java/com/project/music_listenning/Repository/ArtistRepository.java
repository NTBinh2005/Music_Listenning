package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findByNameIgnoreCase(String name);
    // Search artists theo tên
    @Query("""
    SELECT a FROM Artist a
    WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY a.verified DESC, a.name ASC
    """)
    List<Artist> searchByName(@Param("keyword") String keyword, Pageable pageable);

}
