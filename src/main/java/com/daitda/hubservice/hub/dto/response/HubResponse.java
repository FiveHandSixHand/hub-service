package com.daitda.hubservice.hub.dto.response;

import com.daitda.hubservice.hub.entity.Hub;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class HubResponse {

    private UUID hubId;
    private String hubName;
    private String hubAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isCentral;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static HubResponse from(Hub hub) {

        return HubResponse.builder()
                .hubId(hub.getHubId())
                .hubName(hub.getHubName())
                .hubAddress(hub.getHubAddress())
                .latitude(hub.getLatitude())
                .longitude(hub.getLongitude())
                .isCentral(hub.isCentral())
                .createdAt(hub.getCreatedAt())
                .updatedAt(hub.getUpdatedAt())
                .build();
    }
}
