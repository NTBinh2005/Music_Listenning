package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.Impl.AudioStreamService;
import com.project.music_listenning.Service.SongService;
import com.project.music_listenning.dto.response.SongDTO.SongStreamResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final AudioStreamService audioStreamService;
    private final SongService songService;

    /**
     * GET /api/stream/{songId}
     *
     * Endpoint stream audio với Range support.
     * Browser/Howler gửi request, server proxy đến Cloudinary.
     *
     * Headers quan trọng:
     *   Request:  Range: bytes=0-          (lấy từ đầu)
     *             Range: bytes=1048576-    (tua đến byte 1MB)
     *   Response: 206 Partial Content      (thành công Range)
     *             Accept-Ranges: bytes     (báo cho client biết hỗ trợ Range)
     *             Content-Range: bytes 0-1048575/5242880
     */
    @GetMapping("/{songId}")
    public void stream(@PathVariable UUID songId,
                       HttpServletRequest  request,
                       HttpServletResponse response) throws IOException {
        audioStreamService.streamAudio(songId, request, response);
    }

    /**
     * GET /api/stream/{songId}/info
     *
     * Trả về metadata (không có audioUrl gốc) — dùng để hiện thông tin bài
     * trước khi bắt đầu stream. Client dùng endpoint /stream/{id} để phát.
     */
    @GetMapping("/{songId}/info")
    public ResponseEntity<SongStreamResponse> getInfo(@PathVariable UUID songId) {
        return ResponseEntity.ok(songService.getStreamUrl(songId));
    }
}
