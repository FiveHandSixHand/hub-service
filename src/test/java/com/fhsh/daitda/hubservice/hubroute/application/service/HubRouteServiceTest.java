package com.fhsh.daitda.hubservice.hubroute.application.service;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.command.UpdateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.command.HubRouteCommandService;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import com.fhsh.daitda.hubservice.infrastructure.naver.client.NaverDirectionsClient;
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

    @Mock
    private NaverDirectionsClient naverDirectionsClient;

    @InjectMocks
    private HubRouteCommandService hubRouteCommandService;

    private static final UUID HUB_ROUTE_ID = UUID.randomUUID();
    private static final UUID SRC_HUB_ID = UUID.randomUUID();
    private static final UUID DEST_HUB_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("허브 경로 생성 성공")
    void 허브경로생성() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .build();

        Hub srcHub = 생성된허브(SRC_HUB_ID);
        Hub destHub = 생성된허브(DEST_HUB_ID);

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(false);

        // Naver Directions 계산 결과를 mock
        when(naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        )).thenReturn(new NaverDirectionsClient.RouteMetrics(
                95,
                new BigDecimal("123.45")
        ));

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
        assertThat(result.durationTime()).isEqualTo(95);
        assertThat(result.distance()).isEqualByComparingTo("123.45");
        assertThat(result.createdAt()).isNotNull();

        verify(hubRepository).findByHubIdAndDeletedAtIsNull(SRC_HUB_ID);
        verify(hubRepository).findByHubIdAndDeletedAtIsNull(DEST_HUB_ID);
        verify(hubRouteRepository).existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID);
        verify(naverDirectionsClient).getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );
        verify(hubRouteRepository).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("중복된 출발 허브와 도착 허브 조합은 생성할 수 없다")
    void 중복허브경로생성실패() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
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

        verify(naverDirectionsClient, never()).getDrivingMetrics(any(), any(), any(), any());
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
                .build();

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출발 허브와 도착 허브는 같을 수 없습니다.");

        verify(naverDirectionsClient, never()).getDrivingMetrics(any(), any(), any(), any());
        verify(hubRouteRepository, never()).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("허브 경로 생성 시 네이버 길찾기 계산값으로 거리와 시간을 저장한다")
    void 허브경로생성_네이버계산값반영() {
        // given
        UUID srcHubId = UUID.randomUUID();
        UUID destHubId = UUID.randomUUID();
        UUID hubRouteId = UUID.randomUUID();

        Hub srcHub = Hub.create(
                "서울특별시 센터",
                "서울특별시 송파구 송파대로 55",
                new BigDecimal("127.100000"),
                new BigDecimal("37.514000"),
                false,
                USER_ID
        );
        ReflectionTestUtils.invokeMethod(srcHub, "prePersist");
        ReflectionTestUtils.setField(srcHub, "hubId", srcHubId);

        Hub destHub = Hub.create(
                "경기 북부 센터",
                "경기도 고양시 덕양구 권율대로 570",
                new BigDecimal("126.873000"),
                new BigDecimal("37.640000"),
                false,
                USER_ID
        );
        ReflectionTestUtils.invokeMethod(destHub, "prePersist");
        ReflectionTestUtils.setField(destHub, "hubId", destHubId);

        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(srcHubId)
                .destHubId(destHubId)
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(srcHubId))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(destHubId))
                .thenReturn(Optional.of(destHub));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId))
                .thenReturn(false);

        when(naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        )).thenReturn(new NaverDirectionsClient.RouteMetrics(
                95,
                new BigDecimal("123.45")
        ));

        when(hubRouteRepository.saveAndFlush(any(HubRoute.class)))
                .thenAnswer(invocation -> {
                    HubRoute hubRoute = invocation.getArgument(0);
                    ReflectionTestUtils.invokeMethod(hubRoute, "prePersist");
                    ReflectionTestUtils.setField(hubRoute, "hubRouteId", hubRouteId);
                    return hubRoute;
                });

        // when
        FindHubRouteResult result = hubRouteCommandService.createHubRoute(command, USER_ID);

        // then
        assertThat(result.hubRouteId()).isEqualTo(hubRouteId);
        assertThat(result.srcHubId()).isEqualTo(srcHubId);
        assertThat(result.destHubId()).isEqualTo(destHubId);
        assertThat(result.durationTime()).isEqualTo(95);
        assertThat(result.distance()).isEqualByComparingTo("123.45");

        verify(naverDirectionsClient).getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );
    }

    @Test
    @DisplayName("출발 허브가 존재하지 않으면 허브 경로 생성에 실패한다")
    void 허브경로생성실패_출발허브없음() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubErrorCode.HUB_NOT_FOUND);

        verify(naverDirectionsClient, never()).getDrivingMetrics(any(), any(), any(), any());
        verify(hubRouteRepository, never()).saveAndFlush(any(HubRoute.class));
    }

    @Test
    @DisplayName("이미 존재하는 허브 경로는 생성할 수 없다")
    void 허브경로생성실패_중복경로() {
        // given
        UUID srcHubId = UUID.randomUUID();
        UUID destHubId = UUID.randomUUID();

        Hub srcHub = Hub.create(
                "서울특별시 센터",
                "서울특별시 송파구 송파대로 55",
                new BigDecimal("127.100000"),
                new BigDecimal("37.514000"),
                false,
                USER_ID
        );
        ReflectionTestUtils.invokeMethod(srcHub, "prePersist");
        ReflectionTestUtils.setField(srcHub, "hubId", srcHubId);

        Hub destHub = Hub.create(
                "경기 북부 센터",
                "경기도 고양시 덕양구 권율대로 570",
                new BigDecimal("126.873000"),
                new BigDecimal("37.640000"),
                false,
                USER_ID
        );
        ReflectionTestUtils.invokeMethod(destHub, "prePersist");
        ReflectionTestUtils.setField(destHub, "hubId", destHubId);

        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(srcHubId)
                .destHubId(destHubId)
                .build();

        when(hubRepository.findByHubIdAndDeletedAtIsNull(srcHubId))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(destHubId))
                .thenReturn(Optional.of(destHub));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_CONFLICT);

        verify(naverDirectionsClient, never()).getDrivingMetrics(any(), any(), any(), any());
    }

    @Test
    @DisplayName("유니크 제약 위반 시 중복 예외를 반환한다")
    void 허브경로생성_유니크제약위반실패() {
        // given
        CreateHubRouteCommand command = CreateHubRouteCommand.builder()
                .srcHubId(SRC_HUB_ID)
                .destHubId(DEST_HUB_ID)
                .build();

        Hub srcHub = 생성된허브(SRC_HUB_ID);
        Hub destHub = 생성된허브(DEST_HUB_ID);

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));
        when(hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(false);

        // saveAndFlush 전까지는 정상 흐름을 타야 하므로 Naver 계산값도 stub
        when(naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        )).thenReturn(new NaverDirectionsClient.RouteMetrics(
                95,
                new BigDecimal("123.45")
        ));

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

        verify(naverDirectionsClient).getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );
    }

    @Test
    @DisplayName("허브 경로 수정 시 네이버 길찾기 계산값으로 거리와 시간을 재계산한다")
    void 허브경로수정_네이버재계산반영() {
        // given
        UpdateHubRouteCommand command = UpdateHubRouteCommand.builder().build();

        HubRoute hubRoute = HubRoute.create(
                SRC_HUB_ID,
                DEST_HUB_ID,
                10,
                new BigDecimal("10.00"),
                USER_ID
        );
        ReflectionTestUtils.invokeMethod(hubRoute, "prePersist");
        ReflectionTestUtils.setField(hubRoute, "hubRouteId", HUB_ROUTE_ID);

        Hub srcHub = 생성된허브(SRC_HUB_ID);
        Hub destHub = 생성된허브(DEST_HUB_ID);

        when(hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(HUB_ROUTE_ID))
                .thenReturn(Optional.of(hubRoute));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));

        when(naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        )).thenReturn(new NaverDirectionsClient.RouteMetrics(
                77,
                new BigDecimal("101.25")
        ));

        // when
        FindHubRouteResult result = hubRouteCommandService.updateHubRoute(HUB_ROUTE_ID, command, USER_ID);

        // then
        assertThat(result.hubRouteId()).isEqualTo(HUB_ROUTE_ID);
        assertThat(result.durationTime()).isEqualTo(77);
        assertThat(result.distance()).isEqualByComparingTo("101.25");

        verify(naverDirectionsClient).getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );
    }

    @Test
    @DisplayName("수정 대상 허브 경로가 없으면 실패한다")
    void 허브경로수정실패_대상없음() {
        // given
        UpdateHubRouteCommand command = UpdateHubRouteCommand.builder().build();

        when(hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(HUB_ROUTE_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteCommandService.updateHubRoute(HUB_ROUTE_ID, command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);

        verify(naverDirectionsClient, never()).getDrivingMetrics(any(), any(), any(), any());
    }

    private Hub 생성된허브(UUID hubId) {
        Hub hub = Hub.create(
                "테스트 허브",
                "서울특별시 송파구 테스트로 1",
                // 경도(longitude)
                new BigDecimal("126.978000"),
                // 위도(latitude)
                new BigDecimal("37.566500"),
                false,
                USER_ID
        );

        ReflectionTestUtils.setField(hub, "hubId", hubId);
        return hub;
    }
}