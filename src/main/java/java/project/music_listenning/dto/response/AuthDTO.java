package java.project.music_listenning.dto.response;

import jakarta.validation.constraints.NotBlank;

public class AuthDTO {
    public record AuthResponse(
            String accessToken,
            String refreshToken,
            UserInfo user
    ) {}

    public record UserInfo(
            String id,
            String username,
            String email,
            String avatarUrl,
            String role
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

}
