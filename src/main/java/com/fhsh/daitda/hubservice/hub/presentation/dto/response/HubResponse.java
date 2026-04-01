package com.fhsh.daitda.hubservice.hub.presentation.dto.response;

import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
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

    public static HubResponse from(FindHubResult result) {

        return HubResponse.builder()
                .hubId(result.hubId())
                .hubName(result.hubName())
                .hubAddress(result.hubAddress())
                .latitude(result.latitude())
                .longitude(result.longitude())
                .isCentral(result.isCentral())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .build();
    }
}
