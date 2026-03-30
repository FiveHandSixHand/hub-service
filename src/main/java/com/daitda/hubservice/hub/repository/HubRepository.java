package com.daitda.hubservice.hub.repository;

import com.daitda.hubservice.hub.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID> {

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    List<Hub> findAllByDeletedAtIsNull();
}
