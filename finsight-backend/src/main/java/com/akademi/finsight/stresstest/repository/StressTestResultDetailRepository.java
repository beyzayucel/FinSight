package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StressTestResultDetailRepository extends JpaRepository<StressTestResultDetail, UUID> {
}
