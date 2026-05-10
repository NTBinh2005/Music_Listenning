package com.project.music_listenning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class ItunesDto {

    // iTunes trả về wrapper object chứa list results
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(
            int resultCount,
            List<ItunesTrack> results
    ) {}

    // Mỗi track trong results — @JsonIgnoreProperties bỏ qua field không dùng
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItunesTrack(
            String wrapperType,       // "track" | "collection" | "artist"
            String kind,              // "song" — lọc chỉ lấy loại này
            Long trackId,
            String trackName,
            String artistName,
            String collectionName,    // tên album
            String artworkUrl100,     // URL ảnh 100x100 — có thể đổi thành 500x500
            String previewUrl,        // URL audio 30 giây (m4a)
            Long trackTimeMillis,     // thời lượng ms
            Integer trackNumber,
            String primaryGenreName,
            String country
    ) {
        // Lấy ảnh 500x500 thay vì 100x100 — chỉ cần replace URL
        public String artworkUrl500() {
            if (artworkUrl100 == null) return null;
            return artworkUrl100.replace("100x100", "500x500");
        }

        // Convert ms → giây
        public int durationSeconds() {
            if (trackTimeMillis == null) return 0;
            return (int) (trackTimeMillis / 1000);
        }

        // Chỉ lấy track loại "song", bỏ album/artist
        public boolean isSong() {
            return "song".equals(kind);
        }
    }

    // Response trả về sau khi import vào DB
    public record ImportResult(
            int total,      // tổng số track từ iTunes
            int imported,   // số track đã import thành công
            int skipped,    // số track bỏ qua (đã có trong DB hoặc không có previewUrl)
            List<String> importedTitles
    ) {}
}