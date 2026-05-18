package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Song;
import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.RecommendationRepository;
import com.project.music_listenning.dto.response.RecommendationDto.*;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepo;

    private static final int TARGET_SIZE = 20;  // số bài recommend mỗi section

    /**
     * Lấy toàn bộ recommendation cho trang chủ.
     * Trả về nhiều section khác nhau để UI hiện dạng scroll ngang.
     */
    public RecommendationResponse getRecommendations() {
        UUID userId    = getCurrentUserId();
        long historyCount = recommendationRepo.countPlayHistory(userId);

        log.debug("Generating recommendations for userId={}, historyCount={}",
                userId, historyCount);

        // Nếu user chưa có đủ history → trả fallback
        if (historyCount < 5) {
            return buildNewUserResponse(userId);
        }

        List<RecommendationSection> sections = new ArrayList<>();

        // Section 1: "Đề xuất cho bạn" — collaborative
        List<SongResponse> collaborative = fetchUnique(
                recommendationRepo.findCollaborativeRecommendations(userId, TARGET_SIZE)
        );
        if (!collaborative.isEmpty()) {
            sections.add(new RecommendationSection(
                    "Đề xuất cho bạn",
                    "Dựa theo sở thích người dùng tương tự",
                    "collaborative",
                    collaborative
            ));
        }

        // Section 2: "Từ các nghệ sĩ bạn yêu thích" — content-based by artist
        List<SongResponse> byArtist = fetchUnique(
                recommendationRepo.findContentBasedByArtist(userId, TARGET_SIZE)
        );
        if (!byArtist.isEmpty()) {
            sections.add(new RecommendationSection(
                    "Từ nghệ sĩ bạn yêu thích",
                    "Bài mới từ các nghệ sĩ bạn hay nghe",
                    "artist_based",
                    byArtist
            ));
        }

        // Section 3: "Cùng thể loại" — content-based by genre
        List<SongResponse> byGenre = fetchUnique(
                recommendationRepo.findContentBasedByGenre(userId, TARGET_SIZE)
        );
        if (!byGenre.isEmpty()) {
            sections.add(new RecommendationSection(
                    "Cùng thể loại bạn thích",
                    null,
                    "genre_based",
                    byGenre
            ));
        }

        // Section 4: "Bởi vì bạn nghe..." — similar to last played
        List<SongResponse> similar = fetchUnique(
                recommendationRepo.findSimilarToLastPlayed(userId, 10)
        );
        if (!similar.isEmpty()) {
            sections.add(new RecommendationSection(
                    "Vì bạn vừa nghe",
                    similar.get(0).artist().name(),   // tên nghệ sĩ bài vừa nghe
                    "similar",
                    similar
            ));
        }

        // Nếu không đủ sections → thêm fallback
        if (sections.size() < 2) {
            List<SongResponse> fallback = fetchUnique(
                    recommendationRepo.findTopSongsExcludingPlayed(userId, TARGET_SIZE)
            );
            if (!fallback.isEmpty()) {
                sections.add(new RecommendationSection(
                        "Có thể bạn sẽ thích",
                        null,
                        "fallback",
                        fallback
                ));
            }
        }

        return new RecommendationResponse(sections, false);
    }

    /**
     * Response cho user mới — chưa có history.
     * Chỉ trả top songs toàn hệ thống.
     */
    private RecommendationResponse buildNewUserResponse(UUID userId) {
        List<SongResponse> topSongs = fetchUnique(
                recommendationRepo.findTopSongsExcludingPlayed(userId, TARGET_SIZE * 2)
        );

        List<RecommendationSection> sections = List.of(
                new RecommendationSection(
                        "Bài hát nổi bật",
                        "Hãy nghe thêm để nhận đề xuất cá nhân hóa",
                        "top_global",
                        topSongs
                )
        );

        return new RecommendationResponse(sections, true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Convert list Song → SongResponse, đồng thời deduplicate theo id.
     * Dùng LinkedHashMap để giữ thứ tự.
     */
    private List<SongResponse> fetchUnique(List<Song> songs) {
        return songs.stream()
                .collect(Collectors.toMap(
                        Song::getId,
                        SongResponse::from,
                        (a, b) -> a,            // giữ cái đầu nếu trùng id
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof User user) return user.getId();
        throw new IllegalStateException("User chưa đăng nhập");
    }
}
