package com.project.music_listenning.Controller;

import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Service.Impl.ProfileService;
import com.project.music_listenning.dto.response.AuthDTO.UserInfo;
import com.project.music_listenning.dto.response.ProfileDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    /** GET /api/users/me — trả về UserInfo (dùng khi init app) */
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

    /** GET /api/users/me/profile — trả về profile đầy đủ kèm stats */
    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    /** PUT /api/users/me — cập nhật username / avatarUrl */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    /** PUT /api/users/me/password — đổi mật khẩu */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
