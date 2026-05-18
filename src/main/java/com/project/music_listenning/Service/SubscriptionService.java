package com.project.music_listenning.Service;

import com.project.music_listenning.Entity.Subscription;
import com.project.music_listenning.dto.response.SubscriptionDto.SubscriptionResponse;

import java.util.UUID;

public interface SubscriptionService {
    boolean currentUserIsPremium();
    boolean isPremium(UUID userId);
    SubscriptionResponse getMySubscription();
    SubscriptionResponse upgradePremium(UUID userId, Subscription.Plan plan, int months);
    void cancelPremium(UUID userId);
}
