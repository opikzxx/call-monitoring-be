package com.callmonitoring.backend.service;

import com.callmonitoring.backend.dto.request.SentimentFilter;
import com.callmonitoring.backend.dto.response.CallMonitoringResponse;
import com.callmonitoring.backend.dto.response.PageResponse;
import com.callmonitoring.backend.entity.CallMonitoring;
import com.callmonitoring.backend.entity.specification.CallMonitoringSpecification;
import com.callmonitoring.backend.repository.CallMonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CallMonitoringService {
    private static final int PAGE_SIZE = 5;
    private static final Set<String> SORTABLE_FIELDS = Set.of(
        "callId", "callTimestamp", "csName", "customerName", "sentimentScore"
    );

    private final CallMonitoringRepository callMonitoringRepository;

    public PageResponse<CallMonitoringResponse> search(
        String search,
        LocalDate startDate,
        LocalDate endDate,
        SentimentFilter sentiment,
        String sortBy,
        Sort.Direction sortDir,
        int page
    ) {
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sortBy field: " + sortBy);
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        OffsetDateTime from = startDate != null ? startDate.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime to = endDate != null ? endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1) : null;

        Specification<CallMonitoring> spec = CallMonitoringSpecification.withFilters(search, from, to, sentiment);
        var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(sortDir, sortBy));

        return PageResponse.from(callMonitoringRepository.findAll(spec, pageable).map(CallMonitoringResponse::from));
    }
}
