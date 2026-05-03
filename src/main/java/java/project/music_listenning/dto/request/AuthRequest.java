package java.project.music_listenning.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {
    public record RegisterRequest(
            @NotBlank(message = "Username không được để trống")
            @Size(min = 3, max = 50, message = "Username từ 3–50 ký tự")
            String username,

            @NotBlank @Email(message = "Email không hợp lệ")
            String email,

            @NotBlank
            @Size(min = 6, message = "Password tối thiểu 6 ký tự")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}
}
