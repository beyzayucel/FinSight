package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StressTestResultDetailRepository extends JpaRepository<StressTestResultDetail, UUID> {
}
