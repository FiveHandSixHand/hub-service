package com.fhsh.daitda.hubservice.hub.domain.entity;


import com.fhsh.daitda.domain.BaseUserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_hub")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseUserEntity {

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


    @Builder(access = AccessLevel.PRIVATE)
    private Hub(UUID hubId, String hubName, String hubAddress,
                BigDecimal latitude, BigDecimal longitude, boolean isCentral) {

        this.hubId = hubId;
        this.hubName = hubName;
        this.hubAddress = hubAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCentral = isCentral;
    }

    public static Hub create(String hubName, String hubAddress, BigDecimal latitude,
                             BigDecimal longitude, boolean isCentral, UUID createdBy) {

        Hub hub =  Hub.builder()
                .hubName(hubName)
                .hubAddress(hubAddress)
                .latitude(latitude)
                .longitude(longitude)
                .isCentral(isCentral)
                .build();

        hub.createdBy = createdBy;
        return hub;
    }

    @PrePersist
    private void prePersist() {
        if (this.hubId == null) {
            this.hubId = UUID.randomUUID();
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
        delete(deletedBy);
    }

}
