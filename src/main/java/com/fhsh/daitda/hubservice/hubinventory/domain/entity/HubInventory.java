package com.fhsh.daitda.hubservice.hubinventory.domain.entity;

import com.fhsh.daitda.domain.BaseUserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_hub_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hub_inventory_hub_company_product", columnNames = {"hub_id", "company_id", "product_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubInventory extends BaseUserEntity {

    @Id
    @Column(name = "hub_inventory_id", nullable = false, updatable = false)
    private UUID hubInventoryId;

    /*
    * update / decrease / restore처럼 동일 재고 row를 수정하는 경로가 있어서
    * @Version 기반 Optimistic Lock을 적용
    */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private HubInventory(UUID hubInventoryId, UUID hubId, UUID companyId,
                         UUID productId, Integer quantity) {

        this.hubInventoryId = hubInventoryId;
        this.hubId = hubId;
        this.companyId = companyId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static HubInventory create(UUID hubId, UUID companyId, UUID productId,
                                      Integer quantity, UUID createdBy) {

        validateRequiredIds(hubId, companyId, productId);
        validateNonNegativeQuantity(quantity);

        HubInventory hubInventory = HubInventory.builder()
                .hubId(hubId)
                .companyId(companyId)
                .productId(productId)
                .quantity(quantity)
                .build();

        hubInventory.createdBy = createdBy;
        return hubInventory;
    }

    @PrePersist
    private void prePersist() {
        if (this.hubInventoryId == null) {
            this.hubInventoryId = UUID.randomUUID();
        }
    }

    public void updateQuantity(Integer quantity, UUID updatedBy) {
        validateNonNegativeQuantity(quantity);

        this.quantity = quantity;
        this.updatedBy = updatedBy;
    }

    public void decrease(Integer quantity, UUID updatedBy) {
        validatePositiveQuantity(quantity);

        if (this.quantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.quantity -= quantity;
        this.updatedBy = updatedBy;
    }

    public void restoreQuantity(Integer quantity, UUID restoredBy) {
        validatePositiveQuantity(quantity);

        this.quantity += quantity;
        this.updatedBy = restoredBy;
    }

    public void softDelete(UUID deletedBy) {
        delete(deletedBy);
    }

    private static void validateRequiredIds(UUID hubId, UUID companyId, UUID productId) {
        if (hubId == null) {
            throw new IllegalArgumentException("허브 ID는 필수입니다.");
        }
        if (companyId == null) {
            throw new IllegalArgumentException("업체 ID는 필수입니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
    }

    private static void validateNonNegativeQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0보다 작을 수 없습니다.");
        }
    }

    private static void validatePositiveQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("수량은 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }
}
