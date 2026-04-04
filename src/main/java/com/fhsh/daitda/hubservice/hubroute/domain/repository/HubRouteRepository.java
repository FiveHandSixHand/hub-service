package com.fhsh.daitda.hubservice.hubroute.domain.repository;

import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID> {

    Optional<HubRoute> findByHubRouteIdAndDeletedAtIsNull(UUID hubRouteId);

    Optional<HubRoute> findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(UUID srcHubId, UUID destHubId);

    List<HubRoute> findAllByDeletedAtIsNull();

    List<HubRoute> findAllBySrcHubIdAndDeletedAtIsNull(UUID srcHubId);

    List<HubRoute> findAllByDestHubIdAndDeletedAtIsNull(UUID destHubId);

//    중복 방지용 사전 체크
    boolean existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(UUID srcHubId, UUID destHubId);
}
