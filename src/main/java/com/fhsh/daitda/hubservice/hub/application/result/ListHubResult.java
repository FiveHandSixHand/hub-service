package com.fhsh.daitda.hubservice.hub.application.result;

import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;

import java.math.BigDecimal;
import java.util.UUID;

public record ListHubResult(
        UUID hubId,
        String hubName,
        String hubAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean isCentral
) {
    public static ListHubResult from(Hub hub) {
        return new ListHubResult(
                hub.getHubId(),
                hub.getHubName(),
                hub.getHubAddress(),
                hub.getLatitude(),
                hub.getLongitude(),
                hub.isCentral()
        );
    }
}
