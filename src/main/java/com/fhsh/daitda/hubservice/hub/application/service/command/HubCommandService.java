package com.fhsh.daitda.hubservice.hub.application.service.command;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.application.command.CreateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.command.UpdateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 허브 도메인의 쓰기 작업(Create / Update / Delete)을 담당하는 서비스
 *
 * CommandService로 분리한 이유:
 * - 상태를 변경하는 작업만 한 곳에 모아 책임을 명확하게 하기 위해
 * - 조회 로직(Query)과 트랜잭션 성격이 다른 작업을 분리하기 위해
 * - 팀 컨벤션(application/service/command, query 분리)에 맞추기 위해
 */
@Service
@Transactional
public class HubCommandService {

    private final HubRepository hubRepository;

    public HubCommandService(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    /**
     * 허브 생성
     * - 생성은 DB 상태를 변경하는 대표적인 명령 작업이기 때문
     * - createdBy를 받아 audit 값을 남기므로 쓰기 성격이 명확하기 때문
     */
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

    /**
     * 허브 정보 수정
     */
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

    /**
     * 허브 논리 삭제
     */
    @Transactional
    public void deleteHub(UUID hubId, String deletedBy) {
        Hub hub = findActiveHub(hubId);
        hub.softDelete(deletedBy);
    }

    /**
     * 삭제되지 않은 허브 조회
     *
     *  update, delete에서 같은 조회/검증 로직이 반복되기 때문
     *  "활성 허브 조회" 규칙을 한 군데로 모아 중복과 불일치를 막기 위해
     */
    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }
}