package com.project.music_listenning.dto.response;

import java.util.List;

public class RecommendationDto {

    /**
     * Toàn bộ response recommendation — gồm nhiều sections.
     * isNewUser = true khi chưa có đủ history → FE hiện message khuyến khích nghe thêm.
     */
    public record RecommendationResponse(
            List<RecommendationSection> sections,
            boolean                     isNewUser
    ) {}

    /**
     * Một section trong trang chủ, VD:
     *   title    = "Đề xuất cho bạn"
     *   subtitle = "Dựa theo sở thích người dùng tương tự"
     *   type     = "collaborative"
     *   songs    = [...]
     */
    public record RecommendationSection(
            String           title,
            String           subtitle,   // null nếu không có
            String           type,       // dùng để FE phân biệt source
            List<SongDTO.SongResponse> songs
    ) {}
}