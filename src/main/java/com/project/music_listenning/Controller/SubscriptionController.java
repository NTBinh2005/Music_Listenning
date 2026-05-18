package com.project.music_listenning.Controller;

import com.project.music_listenning.Entity.Subscription.Plan;
import com.project.music_listenning.Service.SubscriptionService;
import com.project.music_listenning.dto.response.SubscriptionDto;
import com.project.music_listenning.dto.response.SubscriptionDto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /** GET /api/subscriptions/me — user xem subscription của mình */
    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponse> getMySubscription() {
        return ResponseEntity.ok(subscriptionService.getMySubscription());
    }

    /**
     * POST /api/subscriptions/upgrade — admin upgrade user lên premium
     * Thực tế sẽ được gọi sau khi payment thành công
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> upgrade(
            @RequestBody SubscriptionDto.UpgradeRequest request) {
        Plan plan = Plan.valueOf(request.plan());
        return ResponseEntity.ok(
                subscriptionService.upgradePremium(request.userId(), plan, request.months())
        );
    }

    /** DELETE /api/subscriptions/{userId} — admin hủy premium */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable java.util.UUID userId) {
        subscriptionService.cancelPremium(userId);
        return ResponseEntity.noContent().build();
    }
}

