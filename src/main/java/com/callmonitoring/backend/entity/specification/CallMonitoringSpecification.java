package com.callmonitoring.backend.entity.specification;

import com.callmonitoring.backend.dto.request.SentimentFilter;
import com.callmonitoring.backend.entity.CallMonitoring;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CallMonitoringSpecification {
    private static final int SENTIMENT_THRESHOLD = 70;

    private CallMonitoringSpecification() {
    }

    public static Specification<CallMonitoring> withFilters(
        String search,
        OffsetDateTime callTimestampFrom,
        OffsetDateTime callTimestampTo,
        SentimentFilter sentimentFilter
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("callId")), pattern),
                    cb.like(cb.lower(root.get("csName")), pattern),
                    cb.like(cb.lower(root.get("customerName")), pattern),
                    cb.like(cb.lower(cb.function("str", String.class, root.get("callTimestamp"))), pattern),
                    cb.like(cb.function("str", String.class, root.get("sentimentScore")), pattern)
                ));
            }

            if (callTimestampFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("callTimestamp"), callTimestampFrom));
            }

            if (callTimestampTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("callTimestamp"), callTimestampTo));
            }

            if (sentimentFilter == SentimentFilter.BELOW_70) {
                predicates.add(cb.lessThan(root.get("sentimentScore"), SENTIMENT_THRESHOLD));
            } else if (sentimentFilter == SentimentFilter.AT_LEAST_70) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sentimentScore"), SENTIMENT_THRESHOLD));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
