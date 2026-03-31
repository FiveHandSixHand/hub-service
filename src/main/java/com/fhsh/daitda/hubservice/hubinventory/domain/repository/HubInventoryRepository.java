package com.fhsh.daitda.hubservice.hubinventory.domain.repository;

import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubInventoryRepository extends JpaRepository<HubInventory, UUID> {

    Optional<HubInventory> findByHubInventoryIdAndDeletedAtIsNull(UUID hubInventoryId);

    Optional<HubInventory> findByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(UUID hubId, UUID companyId, UUID productId);

    List<HubInventory> findAllByHubIdAndDeletedAtIsNull(UUID hubId);

    List<HubInventory> findAllByDeletedAtIsNull();

    boolean existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(UUID hubId, UUID companyId, UUID productId);
}
