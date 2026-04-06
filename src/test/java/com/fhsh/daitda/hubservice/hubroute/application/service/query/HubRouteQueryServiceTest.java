package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
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

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubRouteQueryService hubRouteQueryService;

    private static final UUID SRC_HUB_ID = UUID.randomUUID();
    private static final UUID DEST_HUB_ID = UUID.randomUUID();
    private static final UUID VIA_HUB_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("허브 간 경로 조회 시 허브명, 주소, 표시용 시간/거리를 포함한다")
    void 허브간경로조회_허브상세포함() {
        // given
        HubRoute hubRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 45, "28.50");

        Hub srcHub = 생성된허브(
                SRC_HUB_ID,
                "서울특별시 센터",
                "서울특별시 송파구 송파대로 55"
        );

        Hub destHub = 생성된허브(
                DEST_HUB_ID,
                "경기 북부 센터",
                "경기도 고양시 덕양구 권율대로 570"
        );

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(hubRoute));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));

        // when
        FindHubRouteResult result = hubRouteQueryService.searchHubRoute(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(result.srcHubName()).isEqualTo("서울특별시 센터");
        assertThat(result.srcHubAddress()).isEqualTo("서울특별시 송파구 송파대로 55");
        assertThat(result.destHubName()).isEqualTo("경기 북부 센터");
        assertThat(result.destHubAddress()).isEqualTo("경기도 고양시 덕양구 권율대로 570");
        assertThat(result.durationTime()).isEqualTo(45);
        assertThat(result.durationMinutes()).isEqualTo("45분");
        assertThat(result.distance()).isEqualByComparingTo("28.50");
        assertThat(result.distanceKm()).isEqualTo("28.50km");
    }

    @Test
    @DisplayName("직행 경로가 200km 미만이면 1개짜리 리스트 반환")
    void 직행경로_200km미만_리스트1건반환() {
        // given
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 120, "150.00");

        stubHub(SRC_HUB_ID, "서울특별시 센터", "서울특별시 송파구 송파대로 55");
        stubHub(DEST_HUB_ID, "경기 북부 센터", "경기도 고양시 덕양구 권율대로 570");

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directRoute));

        // when
        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(results.get(0).destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(results.get(0).srcHubName()).isEqualTo("서울특별시 센터");
        assertThat(results.get(0).destHubName()).isEqualTo("경기 북부 센터");
        assertThat(results.get(0).durationMinutes()).isEqualTo("120분");
        assertThat(results.get(0).distanceKm()).isEqualTo("150.00km");
    }

    @Test
    @DisplayName("직행 경로가 200km 이상이면 릴레이 경로 리스트를 반환한다")
    void 직행경로_200km이상_릴레이경로반환() {
        // given
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 240, "280.00");
        HubRoute firstRelay = 생성된경로(SRC_HUB_ID, VIA_HUB_ID, 120, "160.00");
        HubRoute secondRelay = 생성된경로(VIA_HUB_ID, DEST_HUB_ID, 90, "120.00");

        stubHub(SRC_HUB_ID, "서울특별시 센터", "서울특별시 송파구 송파대로 55");
        stubHub(VIA_HUB_ID, "대전광역시 센터", "대전 서구 둔산로 100");
        stubHub(DEST_HUB_ID, "대구광역시 센터", "대구 북구 태평로 161");

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
        assertThat(results.get(0).srcHubName()).isEqualTo("서울특별시 센터");
        assertThat(results.get(0).destHubName()).isEqualTo("대전광역시 센터");
        assertThat(results.get(0).durationMinutes()).isEqualTo("120분");
        assertThat(results.get(0).distanceKm()).isEqualTo("160.00km");

        assertThat(results.get(1).srcHubId()).isEqualTo(VIA_HUB_ID);
        assertThat(results.get(1).destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(results.get(1).srcHubName()).isEqualTo("대전광역시 센터");
        assertThat(results.get(1).destHubName()).isEqualTo("대구광역시 센터");
        assertThat(results.get(1).durationMinutes()).isEqualTo("90분");
        assertThat(results.get(1).distanceKm()).isEqualTo("120.00km");
    }

    @Test
    @DisplayName("직행 경로가 200km이면 릴레이 경로 리스트를 반환한다")
    void 직행경로_200km_릴레이경로반환() {
        HubRoute directRoute = 생성된경로(SRC_HUB_ID, DEST_HUB_ID, 180, "200.00");
        HubRoute firstRelay = 생성된경로(SRC_HUB_ID, VIA_HUB_ID, 120, "120.00");
        HubRoute secondRelay = 생성된경로(VIA_HUB_ID, DEST_HUB_ID, 70, "70.00");

        stubHub(SRC_HUB_ID, "서울특별시 센터", "서울특별시 송파구 송파대로 55");
        stubHub(VIA_HUB_ID, "대전광역시 센터", "대전 서구 둔산로 100");
        stubHub(DEST_HUB_ID, "대구광역시 센터", "대구 북구 태평로 161");

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
        HubRoute firstRelay = 생성된경로(SRC_HUB_ID, VIA_HUB_ID, 120, "160.00");
        HubRoute secondRelay = 생성된경로(VIA_HUB_ID, DEST_HUB_ID, 90, "120.00");

        stubHub(SRC_HUB_ID, "서울특별시 센터", "서울특별시 송파구 송파대로 55");
        stubHub(VIA_HUB_ID, "대전광역시 센터", "대전 서구 둔산로 100");
        stubHub(DEST_HUB_ID, "대구광역시 센터", "대구 북구 태평로 161");

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
        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, SRC_HUB_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출발 허브와 도착 허브는 같을 수 없습니다.");
    }

    @Test
    @DisplayName("직행도 없고 릴레이 경로도 없으면 예외가 발생")
    void 경로없음_예외() {
        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.empty());
        when(hubRouteRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of());

        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }

    private void stubHub(UUID hubId, String hubName, String hubAddress) {
        when(hubRepository.findByHubIdAndDeletedAtIsNull(hubId))
                .thenReturn(Optional.of(생성된허브(hubId, hubName, hubAddress)));
    }

    private Hub 생성된허브(UUID hubId, String hubName, String hubAddress) {
        Hub hub = Hub.create(
                hubName,
                hubAddress,
                new BigDecimal("127.100000"),
                new BigDecimal("37.514000"),
                false,
                USER_ID
        );

        ReflectionTestUtils.invokeMethod(hub, "prePersist");
        ReflectionTestUtils.setField(hub, "hubId", hubId);
        return hub;
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