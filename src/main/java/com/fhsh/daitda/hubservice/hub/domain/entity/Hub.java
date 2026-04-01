package com.fhsh.daitda.hubservice.hub.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_hub")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub {

    @Id
    @Column(name = "hub_id", nullable = false, updatable = false)
    private UUID hubId;

    @Column(name = "hub_name", nullable = false, length = 100)
    private String hubName;

    @Column(name = "hub_address", nullable = false, length = 255)
    private String hubAddress;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_central", nullable = false)
    private boolean isCentral;

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
    private Hub(UUID hubId, String hubName, String hubAddress, BigDecimal latitude,
                BigDecimal longitude, boolean isCentral, OffsetDateTime createdAt, UUID createdBy,
                OffsetDateTime updatedAt, UUID updatedBy, OffsetDateTime deletedAt, UUID deletedBy) {

        this.hubId = hubId;
        this.hubName = hubName;
        this.hubAddress = hubAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCentral = isCentral;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static Hub create(String hubName, String hubAddress, BigDecimal latitude,
                             BigDecimal longitude, boolean isCentral, UUID createdBy) {

        return Hub.builder()
                .hubName(hubName)
                .hubAddress(hubAddress)
                .latitude(latitude)
                .longitude(longitude)
                .isCentral(isCentral)
                .createdBy(createdBy)
                .build();
    }

    @PrePersist
    private void prePersist() {
        if (this.hubId == null) {
            this.hubId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public void update(String hubName, String hubAddress, BigDecimal latitude,
                       BigDecimal longitude, Boolean isCentral, UUID updatedBy) {

        this.hubName = hubName;
        this.hubAddress = hubAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCentral = isCentral;
        this.updatedBy = updatedBy;
    }

    public void softDelete(UUID deletedBy) {
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = deletedBy;
    }

}
