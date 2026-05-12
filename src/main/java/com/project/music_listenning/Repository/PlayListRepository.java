package com.project.music_listenning.Repository;

import com.project.music_listenning.Entity.PlayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayListRepository extends JpaRepository<PlayList, UUID> {
    // Lấy playlist của user — sắp theo mới nhất
    List<PlayList> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Lấy playlist public để gợi ý cho user khác
    List<PlayList> findByIsPublicTrueOrderByCreatedAtDesc();

    // Kiểm tra playlist có thuộc về user không (dùng trước khi sửa/xóa)
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
