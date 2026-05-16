package com.project.music_listenning.Controller;

import com.project.music_listenning.Entity.User;
import com.project.music_listenning.dto.response.AuthDTO.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * GET /api/users/me
     * Trả về thông tin user đang đăng nhập.
     * Frontend gọi khi app khởi động để restore user từ token.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().name()
        ));
    }

    /**
     * PUT /api/users/me — cập nhật thông tin cá nhân
     * Dùng sau này khi làm trang profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserInfo> updateMe(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {

        // TODO: implement update profile
        // Hiện tại trả về user hiện tại
        return ResponseEntity.ok(new UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().name()
        ));
    }

    public record UpdateProfileRequest(
            String username,
            String avatarUrl
    ) {}
}
