package com.project.music_listenning.Controller;

import com.project.music_listenning.Service.Impl.RecommendationService;
import com.project.music_listenning.dto.response.RecommendationDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * GET /api/recommendations
     * Trả về các section recommend cho user hiện tại.
     * Cần đăng nhập — SecurityConfig đã cover bởi anyRequest().authenticated()
     */
    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getRecommendations());
    }
}
