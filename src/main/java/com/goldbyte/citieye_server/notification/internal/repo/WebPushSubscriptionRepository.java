package com.goldbyte.citieye_server.notification.internal.repo;

import com.goldbyte.citieye_server.notification.internal.entity.WebPushSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscriptionEntity, Long> {
    List<WebPushSubscriptionEntity> findByCitizenCodeIn(List<String> citizenCodes);
    Optional<WebPushSubscriptionEntity> findFirstByEndpoint(String endpoint);
    void deleteByEndpoint(String endpoint);
}
