package com.daitda.hubservice.hub.service;

import com.daitda.hubservice.hub.dto.request.HubCreateRequest;
import com.daitda.hubservice.hub.dto.request.HubUpdateRequest;
import com.daitda.hubservice.hub.dto.response.HubResponse;
import com.daitda.hubservice.hub.entity.Hub;
import com.daitda.hubservice.hub.repository.HubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;

    public HubService(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    @Transactional
    public HubResponse createHub(HubCreateRequest request, UUID createdBy) {
        Hub hub = Hub.create(
                request.getHubName(),
                request.getHubAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getIsCentral(),
                createdBy
        );

        Hub savedHub = hubRepository.save(hub);
        return HubResponse.from(savedHub);
    }

    public List<HubResponse> getHubs() {
        return hubRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hub -> HubResponse.from(hub))
                .toList();
    }

    public HubResponse getHub(UUID hubId) {
        Hub hub = findActiveHub(hubId);
        return HubResponse.from(hub);
    }

    @Transactional
    public HubResponse updateHub(UUID hubId, HubUpdateRequest request, UUID updatedBy) {
        Hub hub = findActiveHub(hubId);

        hub.update(
                request.getHubName(),
                request.getHubAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getIsCentral(),
                updatedBy
        );
        return HubResponse.from(hub);
    }

    @Transactional
    public void deleteHub(UUID hubId, UUID deletedBy) {
        Hub hub = findActiveHub(hubId);
        hub.softDelete(deletedBy);
    }

    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 허브를 찾을 수 없습니다. hubId=" + hubId));
    }
}
