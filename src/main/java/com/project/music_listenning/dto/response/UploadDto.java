package com.project.music_listenning.dto.response;

public class UploadDto {

    /**
     * Response sau khi upload ảnh thành công.
     * Frontend dùng url để lưu vào form tạo Song/Album.
     */
    public record ImageUploadResponse(
            String url,        // URL ảnh trên Cloudinary (HTTPS)
            String publicId,   // ID để xóa file sau này nếu cần
            int width,
            int height
    ) {}

    /**
     * Response sau khi upload audio thành công.
     * duration là số giây — lưu thẳng vào songs.duration_seconds.
     */
    public record AudioUploadResponse(
            String url,
            String publicId,
            int duration       // giây, Cloudinary tự detect
    ) {}
}