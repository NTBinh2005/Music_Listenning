package com.project.music_listenning.Service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.music_listenning.dto.response.UploadDto.AudioUploadResponse;
import com.project.music_listenning.dto.response.UploadDto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Định dạng được chấp nhận
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final Set<String> ALLOWED_AUDIO_TYPES =
            Set.of("audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/flac");

    // ── Upload ảnh (cover album, avatar nghệ sĩ) ─────────────────────────────

    public ImageUploadResponse uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, 5 * 1024 * 1024L, "ảnh"); // max 5MB

        try {
            // Cloudinary nhận byte[] hoặc File
            byte[] fileBytes = file.getBytes();

            Map result = cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                    // folder để dễ quản lý trong Cloudinary Media Library
                    "folder",          "music-app/images",

                    // public_id tự sinh UUID để tránh trùng tên
                    "public_id",       "img_" + UUID.randomUUID().toString().replace("-", ""),

                    // Tự động resize về 500x500 khi upload — tiết kiệm bandwidth
                    "transformation",  "w_500,h_500,c_fill,q_auto,f_auto"
            ));

            log.info("Upload ảnh thành công: {}", result.get("public_id"));

            return new ImageUploadResponse(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    (int) result.get("width"),
                    (int) result.get("height")
            );

        } catch (IOException e) {
            log.error("Upload ảnh thất bại", e);
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
        }
    }

    // ── Upload audio (bài hát) ────────────────────────────────────────────────

    public AudioUploadResponse uploadAudio(MultipartFile file) {
        validateFile(file, ALLOWED_AUDIO_TYPES, 50 * 1024 * 1024L, "audio"); // max 50MB

        try {
            byte[] fileBytes = file.getBytes();

            Map result = cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                    "folder",       "music-app/audio",
                    "public_id",    "audio_" + UUID.randomUUID().toString().replace("-", ""),

                    // resource_type = "video" vì Cloudinary xếp audio vào nhóm video
                    // Nếu để mặc định "image" sẽ báo lỗi
                    "resource_type", "video"
            ));

            // Cloudinary trả duration theo giây (có thể là Double)
            int duration = ((Number) result.getOrDefault("duration", 0)).intValue();

            log.info("Upload audio thành công: {}, duration: {}s",
                    result.get("public_id"), duration);

            return new AudioUploadResponse(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    duration
            );

        } catch (IOException e) {
            log.error("Upload audio thất bại", e);
            throw new RuntimeException("Upload audio thất bại: " + e.getMessage());
        }
    }

    // ── Xóa file (khi admin xóa bài hát) ─────────────────────────────────────

    public void deleteFile(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", resourceType  // "image" hoặc "video"
            ));
            log.info("Xóa file thành công: {}", publicId);
        } catch (IOException e) {
            // Không throw — xóa file thất bại không nên crash request chính
            log.error("Xóa file thất bại: {}", publicId, e);
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file, Set<String> allowedTypes,
                              long maxBytes, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File " + label + " không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Định dạng " + label + " không hợp lệ. Chấp nhận: " + allowedTypes);
        }

        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "File " + label + " quá lớn. Tối đa: " + (maxBytes / 1024 / 1024) + "MB");
        }
    }
}