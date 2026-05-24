package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioStreamService {

    private final SongRepository songRepository;
    private final SubscriptionService subscriptionService;
    private final RestTemplate          restTemplate;

    private static final int BUFFER_SIZE = 8192;  // 8KB buffer

    /**
     * Stream audio file với Range request support.
     *
     * Luồng:
     *   1. Validate quyền (premium check)
     *   2. Lấy audioUrl từ DB
     *   3. Forward Range header sang Cloudinary/iTunes
     *   4. Pipe response về client
     *
     * Tại sao proxy qua BE thay vì trả URL thẳng?
     *   - Ẩn URL gốc, tránh user chia sẻ link
     *   - Control được access (check premium mỗi lần)
     *   - Có thể log analytics chính xác
     */
    public void streamAudio(UUID songId,
                            HttpServletRequest  request,
                            HttpServletResponse response) throws IOException {

        // 1. Lấy song và check quyền
        Song song = songRepository.findById(songId)
                .filter(Song::isActive)
                .orElse(null);

        if (song == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Bài hát không tồn tại");
            return;
        }

        if (song.isPremium() && !subscriptionService.currentUserIsPremium()) {
            response.sendError(402, "Premium required");
            return;
        }

        String audioUrl = song.getAudioUrl();
        if (audioUrl == null || audioUrl.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Audio URL không tồn tại");
            return;
        }

        // 2. Lấy Range header từ client (nếu có)
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);
        log.debug("Stream songId={}, range={}", songId, rangeHeader);

        // 3. Forward request đến nguồn audio
        proxyAudioRequest(audioUrl, rangeHeader, response);
    }

    private void proxyAudioRequest(String audioUrl,
                                   String rangeHeader,
                                   HttpServletResponse response) throws IOException {
        try {
            // Build headers để gửi đến Cloudinary/iTunes
            HttpHeaders requestHeaders = new HttpHeaders();
            if (rangeHeader != null) {
                requestHeaders.set(HttpHeaders.RANGE, rangeHeader);
            }

            RequestCallback requestCallback = req -> {
                req.getHeaders().addAll(requestHeaders);;
            };

            // Dùng ResponseExtractor để pipe stream trực tiếp
            ResponseExtractor<Void> responseExtractor = clientResponse -> {
                // Copy status code
                int statusCode = clientResponse.getStatusCode().value();

                // 206 = Partial Content (Range request thành công)
                // 200 = Full content (không có Range header)
                response.setStatus(statusCode);

                // Copy headers quan trọng từ upstream
                copyHeader(clientResponse.getHeaders(), response,
                        HttpHeaders.CONTENT_TYPE);
                copyHeader(clientResponse.getHeaders(), response,
                        HttpHeaders.CONTENT_LENGTH);
                copyHeader(clientResponse.getHeaders(), response,
                        HttpHeaders.CONTENT_RANGE);
                copyHeader(clientResponse.getHeaders(), response,
                        HttpHeaders.ACCEPT_RANGES);

                // Header cho phép browser cache và tua nhạc
                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                response.setHeader("Cache-Control", "no-cache");

                // Pipe data
                try (InputStream in  = clientResponse.getBody();
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int    bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    }
                }
                return null;
            };

            restTemplate.execute(
                    URI.create(audioUrl),
                    HttpMethod.GET,
                    requestCallback,
                    responseExtractor
            );

        } catch (Exception e) {
            log.error("Stream error for url={}: {}", audioUrl, e.getMessage());
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Stream thất bại");
            }
        }
    }

    private void copyHeader(HttpHeaders source,
                            HttpServletResponse response,
                            String headerName) {
        String value = source.getFirst(headerName);
        if (value != null) {
            response.setHeader(headerName, value);
        }
    }
}
