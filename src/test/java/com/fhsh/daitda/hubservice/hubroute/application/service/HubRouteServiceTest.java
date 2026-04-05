package com.fhsh.daitda.hubservice.hubroute.application.service;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.command.HubRouteCommandService;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HubRouteServiceTest {

    @Mock
    private HubRouteRepository hubRouteRepository;

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubRouteCommandService hubRouteCommandService;

    private static final UUID HUB_ROUTE_ID = UUID.randomUUID();
    private static final UUID SRC_HUB_ID = UUID.randomUUID();
    private static final UUID DEST_HUB_ID = UUID.randomUUID();
    private static final String USER_ID = "test-user";

    @Test
    @DisplayName("허브 경로 생성 성공")
    void 허브경로생성() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .durationTime(120)
                .distance(new BigDecimal("85.50"))
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(생성된허브(SRC_HUB_ID)));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(생성된허브(DEST_HUB_ID)));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(false);

        when(hubRouteRepository.saveAndFlush(any(HubRoute.class)))
                .thenAnswer(invocationOnMock -> {
                    HubRoute hubRoute = invocationOnMock.getArgument(0);
                    ReflectionTestUtils.invokeMethod(hubRoute, "prePersist");
                    ReflectionTestUtils.setField(hubRoute, "hubRouteId", HUB_ROUTE_ID);
                    ReflectionTestUtils.setField(hubRoute, "createdAt", LocalDateTime.now());
                    return hubRoute;
                });

        // when
        FindHubRouteResult result = hubRouteCommandService.createHubRoute(command, USER_ID);

        // then
        assertThat(result.hubRouteId()).isEqualTo(HUB_ROUTE_ID);
        assertThat(result.srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(result.destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(result.durationTime()).isEqualTo(120);
        assertThat(result.distance()).isEqualByComparingTo("85.50");
        assertThat(result.createdAt()).isNotNull();

        verify(hubRepository).findByHubIdAndDeletedAtIsNull(SRC_HUB_ID);
        verify(hubRepository).findByHubIdAndDeletedAtIsNull(DEST_HUB_ID);
        verify(hubRouteRepository).existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID);
        verify(hubRouteRepository).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("중복된 출발 허브와 도착 허브 조합은 생성할 수 없다")
    void 중복허브경로생성실패() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .durationTime(120)
                .distance(new BigDecimal("85.50"))
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(생성된허브(SRC_HUB_ID)));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(생성된허브(DEST_HUB_ID)));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_CONFLICT);

        verify(hubRouteRepository, never()).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("출발 허브와 도착 허브가 같으면 생성할 수 없다")
    void 자기자신허브경로생성실패() {
        // given
        UUID sameHubId = UUID.randomUUID();

        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(sameHubId)
                .destHubId(sameHubId)
                .durationTime(120)
                .distance(new BigDecimal("85.50"))
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(sameHubId))
                .thenReturn(Optional.of(생성된허브(sameHubId)));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(sameHubId, sameHubId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출발 허브와 도착 허브는 같을 수 없습니다.");

        verify(hubRouteRepository, never()).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("유니크 제약 위반 시 중복 예외를 반환한다")
    void 허브경로생성_유니크제약위반실패() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .durationTime(120)
                .distance(new BigDecimal("85.50"))
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(생성된허브(SRC_HUB_ID)));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(생성된허브(DEST_HUB_ID)));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(false);

        ConstraintViolationException constraintViolationException =
                new ConstraintViolationException(
                        "unique constraint violation",
                        new SQLException("duplicate key"),
                        "uk_hub_route_src_dest"
                );

        when(hubRouteRepository.saveAndFlush(any(HubRoute.class)))
                .thenThrow(new DataIntegrityViolationException("DB 제약 조건 위반", constraintViolationException));

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_CONFLICT);
    }

    private Hub 생성된허브(UUID hubId) {
        Hub hub = Hub.create(
                "테스트 허브",
                "서울특별시 송파구 테스트로 1",
                new BigDecimal("37.566500"),
                new BigDecimal("126.978000"),
                false,
                USER_ID
        );

        ReflectionTestUtils.setField(hub, "hubId", hubId);
        return hub;
    }


}
