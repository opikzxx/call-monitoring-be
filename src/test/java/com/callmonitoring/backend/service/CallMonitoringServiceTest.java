package com.callmonitoring.backend.service;

import com.callmonitoring.backend.dto.response.CallMonitoringResponse;
import com.callmonitoring.backend.dto.response.PageResponse;
import com.callmonitoring.backend.entity.CallMonitoring;
import com.callmonitoring.backend.repository.CallMonitoringRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallMonitoringServiceTest {
    @Mock
    private CallMonitoringRepository callMonitoringRepository;

    private CallMonitoringService callMonitoringService;

    @Test
    void searchRejectsUnsupportedSortField() {
        callMonitoringService = new CallMonitoringService(callMonitoringRepository);

        assertThatThrownBy(() -> callMonitoringService.search(
            null, null, null, null, "unknownField", Sort.Direction.DESC, 0
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchRejectsEndDateBeforeStartDate() {
        callMonitoringService = new CallMonitoringService(callMonitoringRepository);

        assertThatThrownBy(() -> callMonitoringService.search(
            null, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 1), null, "callTimestamp", Sort.Direction.DESC, 0
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchMapsResultsAndUsesAFixedPageSizeOfFive() {
        callMonitoringService = new CallMonitoringService(callMonitoringRepository);

        CallMonitoring entity = new CallMonitoring();
        entity.setId(UUID.randomUUID());
        entity.setCallId("CALL-0001");
        entity.setCallTimestamp(OffsetDateTime.parse("2026-08-01T10:00:00Z"));
        entity.setCsName("Budi Santoso");
        entity.setCustomerName("Ayu Lestari");
        entity.setSentimentScore(85);

        when(callMonitoringRepository.findAll(ArgumentMatchers.<Specification<CallMonitoring>>any(), ArgumentMatchers.any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity)));

        PageResponse<CallMonitoringResponse> result = callMonitoringService.search(
            "budi", null, null, null, "csName", Sort.Direction.ASC, 2
        );

        assertThat(result.content()).hasSize(1);
        CallMonitoringResponse response = result.content().get(0);
        assertThat(response.callId()).isEqualTo("CALL-0001");
        assertThat(response.csName()).isEqualTo("Budi Santoso");
        assertThat(response.sentimentScore()).isEqualTo(85);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(callMonitoringRepository).findAll(ArgumentMatchers.<Specification<CallMonitoring>>any(), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageSize()).isEqualTo(5);
        assertThat(usedPageable.getPageNumber()).isEqualTo(2);
        assertThat(usedPageable.getSort().getOrderFor("csName").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
