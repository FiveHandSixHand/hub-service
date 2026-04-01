package com.fhsh.daitda.hubservice.hub.application.result;

import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FindHubResult(
        UUID hubId,
        String hubName,
        String hubAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean isCentral,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static FindHubResult from(Hub hub) {
        return new FindHubResult(
                hub.getHubId(),
                hub.getHubName(),
                hub.getHubAddress(),
                hub.getLatitude(),
                hub.getLongitude(),
                hub.isCentral(),
                hub.getCreatedAt(),
                hub.getUpdatedAt()
        );
    }
}
