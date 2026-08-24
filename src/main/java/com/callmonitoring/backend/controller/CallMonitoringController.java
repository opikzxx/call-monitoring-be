package com.callmonitoring.backend.controller;

import com.callmonitoring.backend.dto.request.SentimentFilter;
import com.callmonitoring.backend.dto.response.CallMonitoringResponse;
import com.callmonitoring.backend.dto.response.PageResponse;
import com.callmonitoring.backend.service.CallMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/call-monitoring")
@RequiredArgsConstructor
public class CallMonitoringController {
    private final CallMonitoringService callMonitoringService;

    @GetMapping
    public PageResponse<CallMonitoringResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) SentimentFilter sentiment,
        @RequestParam(defaultValue = "callTimestamp") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
        @RequestParam(defaultValue = "0") int page
    ) {
        return callMonitoringService.search(search, startDate, endDate, sentiment, sortBy, sortDir, page);
    }
}
