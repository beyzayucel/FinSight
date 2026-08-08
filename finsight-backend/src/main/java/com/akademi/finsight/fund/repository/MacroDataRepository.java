package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.MacroData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MacroDataRepository extends JpaRepository<MacroData, LocalDate> {

    Optional<MacroData> findFirstByOrderByDateDesc();
}
