package com.project.music_listenning.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Builder.Default
    private Plan plan = Plan.FREE;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    private OffsetDateTime startDate = OffsetDateTime.now();

    @Column(name = "end_date")
    private OffsetDateTime endDate;   // null = FREE (không hết hạn)

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public enum Plan { FREE, PREMIUM_MONTHLY, PREMIUM_YEARLY }

    /**
     * Kiểm tra user có quyền nghe nhạc premium không.
     * Logic: plan không phải FREE + còn hạn (hoặc không có endDate)
     */
    public boolean isPremiumActive() {
        if (plan == Plan.FREE) return false;
        if (!isActive) return false;
        if (endDate == null) return true;
        return OffsetDateTime.now().isBefore(endDate);
    }
}
