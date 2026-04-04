package com.fhsh.daitda.hubservice.hubinventory.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.result.ListHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;
import com.fhsh.daitda.hubservice.hubinventory.domain.exception.HubInventoryErrorCode;
import com.fhsh.daitda.hubservice.hubinventory.domain.repository.HubInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubInventoryQueryService {

    private final HubInventoryRepository hubInventoryRepository;

    public HubInventoryQueryService(HubInventoryRepository hubInventoryRepository) {
        this.hubInventoryRepository = hubInventoryRepository;
    }

    // 특정 재고 조회
    public FindHubInventoryResult getHubInventory(UUID hubInventoryId) {
        HubInventory hubInventory = findActiveHubInventory(hubInventoryId);
        return FindHubInventoryResult.from(hubInventory);
    }

    // 전체 재고 목록 조회
    public List<ListHubInventoryResult> getHubInventories() {
        return hubInventoryRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hubInventory -> ListHubInventoryResult.from(hubInventory))
                .toList();
    }

    // 특정 허브 기준 재고 조회
    public List<ListHubInventoryResult> getHubInventoriesByHub(UUID hubId) {
        return hubInventoryRepository.findAllByHubIdAndDeletedAtIsNull(hubId)
                .stream()
                .map(hubInventory -> ListHubInventoryResult.from(hubInventory))
                .toList();
    }

    // hubId + companyId + productId 유니크 조합으로 특정 허브 재고 조회
    public FindHubInventoryResult searchHubInventory(UUID hubId, UUID companyId, UUID productId) {
        HubInventory hubInventory = hubInventoryRepository
                .findByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(hubId, companyId, productId)
                .orElseThrow(() -> new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_NOT_FOUND));

        return FindHubInventoryResult.from(hubInventory);
    }

    // 삭제 안 된 재고 row만 찾기
    private HubInventory findActiveHubInventory(UUID hubInventoryId) {
        return hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(hubInventoryId)
                .orElseThrow(() -> new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_NOT_FOUND));
    }
}
