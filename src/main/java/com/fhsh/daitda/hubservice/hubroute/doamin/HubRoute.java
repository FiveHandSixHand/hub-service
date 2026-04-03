package com.fhsh.daitda.hubservice.hubroute.doamin;

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
@Table(name = "p_hub_route", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hub_route_src_dest", columnNames = {"src_hub_id", "dext_hub_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRoute extends BaseUserEntity {

    @Id
    @Column(name = "hub_route_id", nullable = false, updatable = false)
    private UUID hubRouteId;

    @Column(name = "src_hub_id", nullable = false)
    private UUID srcHubId;

    @Column(name = "dest_hub_id", nullable = false)
    private UUID destHubId;

    @Column(name = "duration_time", nullable = false)
    private Integer durationTime;

    @Column(name = "distance", nullable = false, precision = 6, scale = 2)
    private BigDecimal distance;

    @Builder(access = AccessLevel.PRIVATE)
    private HubRoute(UUID hubRouteId, UUID srcHubId, UUID destHubId,
                     Integer durationTime, BigDecimal distance) {
        this.hubRouteId = hubRouteId;
        this.srcHubId = srcHubId;
        this.destHubId = destHubId;
        this.durationTime = durationTime;
        this.distance = distance;
    }

    public static HubRoute create(UUID srcHubId, UUID destHubId,
                                  Integer durationTime, BigDecimal distance,
                                  String createdBy) {
        validateHubIds(srcHubId, destHubId);
        validateDurationTime(durationTime);
        validateDistance(distance);

        HubRoute hubRoute = HubRoute.builder()
                .srcHubId(srcHubId)
                .destHubId(destHubId)
                .durationTime(durationTime)
                .distance(distance)
                .build();

        hubRoute.createdBy = createdBy;
        return hubRoute;
    }

    @PrePersist
    private void prePersist() {
        if (this.hubRouteId == null) {
            this.hubRouteId = UUID.randomUUID();
        }
    }

    public void updateRouteInfo(Integer durationTime, BigDecimal distance, String updatedBy) {
        validateDurationTime(durationTime);
        validateDistance(distance);

        this.durationTime = durationTime;
        this.distance = distance;
        this.updatedBy = updatedBy;
    }

    public void softDelete(String deletedBy) {
        delete(deletedBy);
    }

    private static void validateHubIds(UUID srcHubId, UUID destHubId) {
        if (srcHubId == null) {
            throw new IllegalArgumentException("출발 허브 ID는 필수입니다.");
        }
        if (destHubId == null) {
            throw new IllegalArgumentException("도착 허브 ID는 필수입니다.");
        }
        if (srcHubId.equals(destHubId)) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 같을 수 없습니다.");
        }
    }

    private static void validateDurationTime(Integer durationTime) {
        if (durationTime == null) {
            throw new IllegalArgumentException("소요 시간은 필수입니다.");
        }
        if (durationTime <= 0) {
            throw new IllegalArgumentException("소요 시간은 0보다 커야 합니다.");
        }
    }

    private static void validateDistance(BigDecimal distance) {
        if (distance == null) {
            throw new IllegalArgumentException("이동 거리는 필수입니다.");
        }
        if (distance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("이동 거리는 0보다 커야 합니다.");
        }
    }


}
