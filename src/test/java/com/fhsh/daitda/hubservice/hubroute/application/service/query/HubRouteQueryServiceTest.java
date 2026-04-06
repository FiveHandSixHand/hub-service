package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HubRouteQueryServiceTest {

    @Mock
    private HubRouteRepository hubRouteRepository;

    @InjectMocks
    private HubRouteQueryService hubRouteQueryService;

    private static final UUID SRC_HUB_ID = UUID.randomUUID();
    private static final UUID DEST_HUB_ID = UUID.randomUUID();
    private static final UUID VIA_HUB_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("직행 경로가 200km 미만이면 1개짜리 리스트 반환")
    void 직행경로_200km미만_리스트1건반환() {
        // given
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 120, "150.00");

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directRoute));

        // when
        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(results.get(0).destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(results.get(0).distance()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("직행 경로가 200km 이상이면 릴레이 경로 리스트를 반환한다")
    void 직행경로_200km이상_릴레이경로반환() {
        // given
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 240, "280.00");
        HubRoute firstRelay = 생성된경로(SRC_HUB_ID, VIA_HUB_ID, 120, "160.00");
        HubRoute secondRelay = 생성된경로(VIA_HUB_ID, DEST_HUB_ID, 90, "120.00");

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directRoute));

        when(hubRouteRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(directRoute, firstRelay, secondRelay));

        // when
        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(results.get(0).destHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).srcHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).destHubId()).isEqualTo(DEST_HUB_ID);
    }

    @Test
    @DisplayName("직행 경로가 200km이면 릴레이 경로 리스트를 반환한다")
    void 직행경로_200km_릴레이경로반환() {
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 180, "200.00");
        HubRoute firstRelay = 생성된경로(SRC_HUB_ID, VIA_HUB_ID, 120, "120.00");
        HubRoute secondRelay = 생성된경로(VIA_HUB_ID, DEST_HUB_ID, 70, "70.00");

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directRoute));

        when(hubRouteRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(directRoute, firstRelay, secondRelay));

        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(results.get(0).destHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).srcHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).destHubId()).isEqualTo(DEST_HUB_ID);
    }

    @Test
    @DisplayName("직행 경로가 없더라도 릴레이 경로가 있으면 리스트를 반환한다")
    void 직행경로없음_릴레이경로반환() {
        // given
        HubRoute firstRelay = 생성된경로(
                SRC_HUB_ID,
                VIA_HUB_ID,
                120,
                "160.00"
        );

        HubRoute secondRelay = 생성된경로(
                VIA_HUB_ID,
                DEST_HUB_ID,
                90,
                "120.00"
        );

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.empty());

        when(hubRouteRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(firstRelay, secondRelay));

        // when
        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(results.get(0).destHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).srcHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).destHubId()).isEqualTo(DEST_HUB_ID);
    }

    @Test
    @DisplayName("출발 허브와 도착 허브가 같으면 예외 발생")
    void 출발도착허브동일_예외() {
        // when & then
        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, SRC_HUB_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출발 허브와 도착 허브는 같을 수 없습니다.");
    }

    @Test
    @DisplayName("직행도 없고 릴레이 경로도 없으면 예외가 발생")
    void 경로없음_예외() {
        // given
        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.empty());

        when(hubRouteRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }

    private HubRoute 생성된경로(UUID srcHubId, UUID destHubId, Integer durationTime, String distance) {
        HubRoute hubRoute = HubRoute.create(
                srcHubId,
                destHubId,
                durationTime,
                new BigDecimal(distance),
                USER_ID
        );

        ReflectionTestUtils.invokeMethod(hubRoute, "prePersist");
        ReflectionTestUtils.setField(hubRoute, "createdAt", LocalDateTime.now());

        return hubRoute;
    }
}
