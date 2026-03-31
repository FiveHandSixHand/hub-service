package com.fhsh.daitda.hubservice.hubinventory.application.service;

import com.fhsh.daitda.hubservice.hubinventory.application.dto.command.CreateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.dto.command.DecreaseHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.dto.command.RestoreHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.dto.command.UpdateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.result.ListHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;
import com.fhsh.daitda.hubservice.hubinventory.domain.repository.HubInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubInventoryService {

    private final HubInventoryRepository hubInventoryRepository;

    public HubInventoryService(HubInventoryRepository hubInventoryRepository) {
        this.hubInventoryRepository = hubInventoryRepository;
    }

    // 재고생성
    @Transactional
    public FindHubInventoryResult createHubInventory(CreateHubInventoryCommand command, UUID createdBy) {
        validateDuplicateHubInventory(command.getHubId(), command.getCompanyId(), command.getProductId());

        HubInventory hubInventory = HubInventory.create(
                command.getHubId(),
                command.getCompanyId(),
                command.getProductId(),
                command.getQuantity(),
                createdBy
        );

        HubInventory savedHubInventory = hubInventoryRepository.save(hubInventory);
        return FindHubInventoryResult.from(savedHubInventory);
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
                .orElseThrow(() -> new IllegalArgumentException("해당 허브 재고를 찾을 수 없습니다."));

        return FindHubInventoryResult.from(hubInventory);
    }

    // 재고 수량 수정
    @Transactional
    public FindHubInventoryResult updateHubInventory(UUID hubInventoryId, UpdateHubInventoryCommand command, UUID updatedBy) {
        HubInventory hubInventory = findActiveHubInventory(hubInventoryId);

        hubInventory.updateQuantity(command.getQuantity(), updatedBy);

        return FindHubInventoryResult.from(hubInventory);
    }

    // 재고 차감
    @Transactional
    public FindHubInventoryResult decreaseHubInventory(DecreaseHubInventoryCommand command, UUID updatedBy) {
        HubInventory hubInventory = findActiveHubInventory(command.getHubInventoryId());

        hubInventory.decrease(command.getQuantity(), updatedBy);

        return FindHubInventoryResult.from(hubInventory);
    }

    // 재고 복원
    @Transactional
    public FindHubInventoryResult restoreHubInventory(RestoreHubInventoryCommand command, UUID updatedBy) {
        HubInventory hubInventory = findActiveHubInventory(command.getHubInventoryId());

        hubInventory.restore(command.getQuantity(), updatedBy);

        return FindHubInventoryResult.from(hubInventory);
    }

    // 논리 삭제
    @Transactional
    public void deleteHubInventory(UUID hubInventoryId, UUID deletedBy) {
        HubInventory hubInventory = findActiveHubInventory(hubInventoryId);
        hubInventory.softDelete(deletedBy);
    }

    // 중복 체크
    private void validateDuplicateHubInventory(UUID hubId, UUID companyId, UUID productId) {
        boolean exists = hubInventoryRepository.existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(hubId, companyId, productId);

        if (exists) {
            throw new IllegalArgumentException("이미 등록된 허브 재고입니다.");
        }
    }

    // 삭제 안 된 재고 row만 찾기
    private HubInventory findActiveHubInventory(UUID hubInventoryId) {
        return hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(hubInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 허브 재고를 찾을 수 없습니다."));
    }
}
