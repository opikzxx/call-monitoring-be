package com.callmonitoring.backend.repository;

import com.callmonitoring.backend.entity.CallMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CallMonitoringRepository
    extends JpaRepository<CallMonitoring, UUID>, JpaSpecificationExecutor<CallMonitoring> {
}
