package com.fhsh.daitda.hubservice.hubinventory.application.service.command;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubinventory.application.command.*;
import com.fhsh.daitda.hubservice.hubinventory.application.result.DecreaseHubInventoriesByProductResult;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;
import com.fhsh.daitda.hubservice.hubinventory.domain.exception.HubInventoryErrorCode;
import com.fhsh.daitda.hubservice.hubinventory.domain.repository.HubInventoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// 허브 재고 도메인의 쓰기 작업(Create /Update/Delete)을 담당
@Service
@Transactional
public class HubInventoryCommandService {

    private final HubInventoryRepository hubInventoryRepository;

    public HubInventoryCommandService(HubInventoryRepository hubInventoryRepository) {
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

        try {
            HubInventory savedHubInventory = hubInventoryRepository.saveAndFlush(hubInventory);
            return FindHubInventoryResult.from(savedHubInventory);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_CONFLICT);
        }
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

    public List<FindHubInventoryResult> decreaseHubInventories(DecreaseHubInventoriesCommand command, UUID updatedBy) {
        return command.getItems().stream()
                .map(item -> {
                    HubInventory hubInventory = findActiveHubInventory(item.getHubInventoryId());
                    hubInventory.decrease(item.getQuantity(), updatedBy);
                    return FindHubInventoryResult.from(hubInventory);
                })
                .toList();
    }

    public DecreaseHubInventoriesByProductResult decreaseHubInventoriesByProduct(DecreaseHubInventoriesByProductCommand command, UUID updatedBy) {

        List<DecreaseHubInventoriesByProductResult.Item> items = command.getOrderItems().stream()
                .map(orderItem -> {
                    // 회사 + 상품 기준으로 실제 재고 조회
                    HubInventory hubInventory = hubInventoryRepository
                            .findByCompanyIdAndProductIdAndDeletedAtIsNull(
                                    command.getSupplierCompanyId(),
                                    orderItem.getProductId()
                            )
                            .orElseThrow(() -> new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_NOT_FOUND));

                    // 실제 재고 수량 차감
                    hubInventory.decrease(orderItem.getQuantity(), updatedBy);

                    // 어떤 row 사용했는지 결과 반환
                    return DecreaseHubInventoriesByProductResult.Item.builder()
                            .hubInventoryId(hubInventory.getHubInventoryId())
                            .productId(hubInventory.getProductId())
                            .build();
                })
                .toList();

        return DecreaseHubInventoriesByProductResult.builder()
                .items(items)
                .build();
    }

    // 재고 복원
    @Transactional
    public void restoreHubInventories(RestoreHubInventoriesCommand command, UUID updatedBy) {
        command.getOrderItems().forEach(orderItem -> {
            // 실제 복원 대상 재고 조회
            HubInventory hubInventory = findActiveHubInventory(orderItem.getHubInventoryId());
            // 조회한 재고 수량 복원
            hubInventory.restoreQuantity(orderItem.getQuantity(), updatedBy);
        });
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
            throw new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_CONFLICT);
        }
    }

    // 삭제 안 된 재고 row만 찾기
    private HubInventory findActiveHubInventory(UUID hubInventoryId) {
        return hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(hubInventoryId)
                .orElseThrow(() -> new BusinessException(HubInventoryErrorCode.HUB_INVENTORY_NOT_FOUND));
    }
}
