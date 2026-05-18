package com.project.music_listenning.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Bắt tất cả exception và trả về JSON thay vì HTML error page mặc định.
 * Nếu không có class này, Spring trả HTML → React không đọc được message.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Sai email/password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Email hoặc mật khẩu không đúng"));
    }

    // Tài khoản bị vô hiệu hóa
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabled(DisabledException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Tài khoản đã bị vô hiệu hóa"));
    }

    // Lỗi validate @Valid (username/email/password không đúng format)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }

    // Lỗi logic (email đã tồn tại, v.v.) — ném từ AuthService
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    // Fallback — lỗi không xác định
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        log.error("Unhandled exception: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Lỗi server, thử lại sau"));
    }

    @ExceptionHandler(PremiumRequiredException.class)
    public ResponseEntity<Map<String, String>> handlePremiumRequired(
            PremiumRequiredException e) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)   // 402
                .body(Map.of(
                        "message", e.getMessage(),
                        "code",    "PREMIUM_REQUIRED"      // FE dùng code này để hiện modal
                ));
    }
}
