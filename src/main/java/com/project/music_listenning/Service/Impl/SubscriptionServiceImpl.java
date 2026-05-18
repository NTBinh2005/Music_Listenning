package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Subscription;
import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.SubscriptionRepository;
import com.project.music_listenning.Service.SubscriptionService;
import com.project.music_listenning.dto.response.SubscriptionDto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    /** Kiểm tra user hiện tại có premium không — dùng trong SongService */
    @Override
    public boolean currentUserIsPremium() {
        User user = getCurrentUser();
        return subscriptionRepository.findByUserId(user.getId())
                .map(Subscription::isPremiumActive)
                .orElse(false);
    }

    /** Kiểm tra theo userId — dùng trong các service khác */
    @Override
    public boolean isPremium(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(Subscription::isPremiumActive)
                .orElse(false);
    }

    /** Lấy thông tin subscription của user hiện tại */
    @Override
    public SubscriptionResponse getMySubscription() {
        User user = getCurrentUser();
        Subscription sub = subscriptionRepository.findByUserId(user.getId())
                .orElse(null);

        if (sub == null) {
            return new SubscriptionResponse(
                    "FREE", false, null, null
            );
        }

        return new SubscriptionResponse(
                sub.getPlan().name(),
                sub.isPremiumActive(),
                sub.getStartDate(),
                sub.getEndDate()
        );
    }

    /**
     * Upgrade lên Premium — trong thực tế cần tích hợp payment gateway.
     * Hiện tại để đơn giản: admin gọi API này để set premium cho user.
     */
    @Transactional
    @Override
    public SubscriptionResponse upgradePremium(UUID userId, Subscription.Plan plan, int months) {
        User userRef = new User();
        userRef.setId(userId);

        Subscription sub = subscriptionRepository.findByUserId(userId)
                .orElse(Subscription.builder().user(userRef).build());

        sub.setPlan(plan);
        sub.setActive(true);
        sub.setStartDate(OffsetDateTime.now());
        sub.setEndDate(OffsetDateTime.now().plusMonths(months));

        subscriptionRepository.save(sub);

        return new SubscriptionResponse(
                sub.getPlan().name(),
                sub.isPremiumActive(),
                sub.getStartDate(),
                sub.getEndDate()
        );
    }

    /** Hủy premium — đặt endDate về hiện tại */
    @Transactional
    @Override
    public void cancelPremium(UUID userId) {
        subscriptionRepository.findByUserId(userId).ifPresent(sub -> {
            sub.setEndDate(OffsetDateTime.now())
            ;            sub.setActive(false);
            subscriptionRepository.save(sub);
        });
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof User user) return user;
        throw new IllegalStateException("User chưa đăng nhập");
    }
}
