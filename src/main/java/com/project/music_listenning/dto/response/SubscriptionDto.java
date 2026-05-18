package com.project.music_listenning.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SubscriptionDto {

    public record SubscriptionResponse(
            String          plan,          // "FREE" | "PREMIUM_MONTHLY" | "PREMIUM_YEARLY"
            boolean         isPremium,     // true nếu đang active premium
            OffsetDateTime  startDate,
            OffsetDateTime  endDate        // null nếu FREE
    ) {}

    // Admin dùng để upgrade user
    public record UpgradeRequest(
            UUID   userId,
            String plan,    // "PREMIUM_MONTHLY" | "PREMIUM_YEARLY"
            int    months   // số tháng
    ) {}
}
