package com.fhsh.daitda.hubservice.hub.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.application.result.ListHubResult;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 허브 도메인의 조회(Read) 작업을 담당하는 서비스
 *
 * QueryService로 분리한 이유:
 * - 상태를 변경하지 않는 조회 작업만 한 곳에 모아 책임을 명확하게 하기 위해
 * - 쓰기 작업(Command)과 트랜잭션 속성을 분리하기 위해
 * - 팀 컨벤션(application/service/query)에 맞추기 위해
 */
@Service
@Transactional(readOnly = true)
public class HubQueryService {

    private final HubRepository hubRepository;

    public HubQueryService(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    /**
     * 삭제되지 않은 전체 허브 목록 조회
     */
    public List<ListHubResult> getHubs() {
        return hubRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hub -> ListHubResult.from(hub))
                .toList();
    }

//    허브 ID 기준으로 삭제되지 않은 허브 단건 조회
    public FindHubResult getHub(UUID hubId) {
        Hub hub = findActiveHub(hubId);
        return FindHubResult.from(hub);
    }

    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }
}
