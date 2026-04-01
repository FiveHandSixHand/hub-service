package com.fhsh.daitda.hubservice.hub.presentation.dto.response;

import com.fhsh.daitda.hubservice.hub.application.result.ListHubResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ListHubResponse {

    private UUID hubId;
    private String hubName;
    private String hubAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isCentral;

    public static ListHubResponse from(ListHubResult result) {
        return ListHubResponse.builder()
                .hubId(result.hubId())
                .hubName(result.hubName())
                .hubAddress(result.hubAddress())
                .latitude(result.latitude())
                .longitude(result.longitude())
                .isCentral(result.isCentral())
                .build();
    }
}
