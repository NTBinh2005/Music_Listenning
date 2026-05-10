package com.project.music_listenning.Controller;


import com.project.music_listenning.Service.Impl.ItunesImportService;
import com.project.music_listenning.dto.response.ItunesDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // toàn bộ controller chỉ ADMIN dùng được
public class ItunesImportController {

    private final ItunesImportService importService;

    /**
     * POST /api/admin/import/itunes
     *
     * Body:
     * {
     *   "keyword": "son tung",
     *   "limit": 20,
     *   "country": "VN"
     * }
     */
    @PostMapping("/itunes")
    public ResponseEntity<ImportResult> importFromItunes(
            @RequestBody ImportRequest request) {

        ImportResult result = importService.searchAndImport(
                request.keyword(),
                request.limit() > 0 ? request.limit() : 20,
                request.country() != null ? request.country() : "VN"
        );

        return ResponseEntity.ok(result);
    }

    public record ImportRequest(String keyword, int limit, String country) {}
}
