package com.daitda.hubservice.hubinventory.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_hub_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hub_inventory_hub_company_product", columnNames = {"hub_id", "company_id", "product_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubInventory {

    @Id
    @Column(name = "hub_inventory_id", nullable = false, updatable = false)
    private UUID hubInventoryId;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private HubInventory(UUID hubInventoryId, UUID hubId, UUID companyId, UUID productId,
                         Integer quantity, OffsetDateTime createdAt, UUID createdBy, OffsetDateTime updatedAt,
                         UUID updatedBy, OffsetDateTime deletedAt, UUID deletedBy) {

        this.hubInventoryId = hubInventoryId;
        this.hubId = hubId;
        this.companyId = companyId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static HubInventory create(UUID hubId, UUID companyId, UUID productId,
                                      Integer quantity, UUID createdBy) {

        validateRequiredIds(hubId, companyId, productId);
        validateNonNegativeQuantity(quantity);

        return HubInventory.builder()
                .hubId(hubId)
                .companyId(companyId)
                .productId(productId)
                .quantity(quantity)
                .createdBy(createdBy)
                .build();
    }

    @PrePersist
    private void prePersist() {
        if (this.hubInventoryId == null) {
            this.hubInventoryId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public void updateQuantity(Integer quantity, UUID updatedBy) {
        validateNonNegativeQuantity(quantity);

        this.quantity = quantity;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void decrease(Integer quantity, UUID updatedBy) {
        validatePositiveQuantity(quantity);

        if (this.quantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.quantity -= quantity;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void restore(Integer quantity, UUID updatedBy) {
        validatePositiveQuantity(quantity);

        this.quantity += quantity;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void softDelete(UUID deletedBy) {
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = deletedBy;
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
