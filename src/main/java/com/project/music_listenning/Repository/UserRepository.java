package com.project.music_listenning.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.music_listenning.Entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
