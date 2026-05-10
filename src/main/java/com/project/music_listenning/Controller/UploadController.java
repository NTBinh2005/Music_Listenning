package com.project.music_listenning.Controller;



import com.project.music_listenning.Service.Impl.CloudinaryService;
import com.project.music_listenning.dto.response.UploadDto.AudioUploadResponse;
import com.project.music_listenning.dto.response.UploadDto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    /**
     * POST /api/upload/image
     * Content-Type: multipart/form-data
     * field name: "file"
     *
     * Chỉ ADMIN mới upload được — dùng @PreAuthorize thay vì config trong SecurityConfig
     * vì dễ đọc hơn khi nhìn vào controller
     */
    @PostMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(cloudinaryService.uploadImage(file));
    }

    /**
     * POST /api/upload/audio
     * Content-Type: multipart/form-data
     * field name: "file"
     */
    @PostMapping("/audio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AudioUploadResponse> uploadAudio(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(cloudinaryService.uploadAudio(file));
    }

    /**
     * DELETE /api/upload?publicId=xxx&type=image
     * Xóa file trên Cloudinary — dùng khi admin xóa bài hát/album
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFile(
            @RequestParam String publicId,
            @RequestParam(defaultValue = "image") String type) {
        cloudinaryService.deleteFile(publicId, type);
        return ResponseEntity.noContent().build();
    }
}