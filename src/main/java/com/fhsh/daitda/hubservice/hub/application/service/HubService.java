package com.fhsh.daitda.hubservice.hub.application.service;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.application.command.CreateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.command.UpdateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.application.result.ListHubResult;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public FindHubResult createHub(CreateHubCommand command, String createdBy) {
        Hub hub = Hub.create(
                command.getHubName(),
                command.getHubAddress(),
                command.getLatitude(),
                command.getLongitude(),
                command.getIsCentral(),
                createdBy
        );

        Hub savedHub = hubRepository.save(hub);
        return FindHubResult.from(savedHub);
    }

    public List<ListHubResult> getHubs() {
        return hubRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hub -> ListHubResult.from(hub))
                .toList();
    }

    public FindHubResult getHub(UUID hubId) {
        Hub hub = findActiveHub(hubId);
        return FindHubResult.from(hub);
    }

    @Transactional
    public FindHubResult updateHub(UUID hubId, UpdateHubCommand command, String updatedBy) {
        Hub hub = findActiveHub(hubId);

        hub.update(
                command.getHubName(),
                command.getHubAddress(),
                command.getLatitude(),
                command.getLongitude(),
                command.getIsCentral(),
                updatedBy
        );
        return FindHubResult.from(hub);
    }

    @Transactional
    public void deleteHub(UUID hubId, String deletedBy) {
        Hub hub = findActiveHub(hubId);
        hub.softDelete(deletedBy);
    }

    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }
}
