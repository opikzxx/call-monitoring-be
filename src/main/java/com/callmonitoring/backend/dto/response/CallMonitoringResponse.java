package com.callmonitoring.backend.dto.response;

import com.callmonitoring.backend.entity.CallMonitoring;

import java.time.OffsetDateTime;

public record CallMonitoringResponse(
    String callId,
    OffsetDateTime callTimestamp,
    String csName,
    String customerName,
    Integer sentimentScore
) {
    public static CallMonitoringResponse from(CallMonitoring entity) {
        return new CallMonitoringResponse(
            entity.getCallId(),
            entity.getCallTimestamp(),
            entity.getCsName(),
            entity.getCustomerName(),
            entity.getSentimentScore()
        );
    }
}
