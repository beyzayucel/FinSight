package com.akademi.finsight.ai.repository;

import com.akademi.finsight.ai.entity.PortfolioStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioStateRepository extends JpaRepository<PortfolioStateEntity, UUID> {

    /** Sistemde tek bir aktif portföy durumu varsayılır; en güncel kaydı döner. */
    Optional<PortfolioStateEntity> findFirstByOrderByUpdatedAtDesc();
}
