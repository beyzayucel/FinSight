package com.akademi.finsight.decisionhistory.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.decisionhistory.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.decisionhistory.entity.DecisionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RequestMapping(ApiEndpoints.AdminDecisions.BASE)
@Tag(name = "Admin Decision Report", description = "Yönetim Paneli — tüm kullanıcıların yatırım kararlarının salt okunur raporu")
public interface AdminDecisionApi {

    @Operation(
            summary = "Get decision report (Admin only)",
            description = "Tüm kullanıcıların/fonların AI ve manuel kararlarını, opsiyonel kullanıcı/tip/tarih " +
                          "aralığı filtresiyle en yeniden en eskiye sıralı döndürür. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Decision report retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @GetMapping
    ResponseEntity<ApiStandardResponse<List<AdminDecisionRecordResponse>>> getDecisionReport(
            @Parameter(description = "Belirli bir kullanıcıya göre filtrele") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Karar tipine göre filtrele") @RequestParam(required = false) DecisionType type,
            @Parameter(description = "Son N gün (7/30). Boş bırakılırsa tüm zamanlar.") @RequestParam(required = false) Integer days
    );

    @Operation(
            summary = "Get decision detail (Admin only)",
            description = "Rapor tablosundaki bir kararın tam detayını — gerekçe/not, kategori ve hisse " +
                          "ağırlıkları, performans metrikleri ve iliştirilmiş stres testi — döndürür. " +
                          "Kullanıcının kendi Karar Geçmişi ekranıyla aynı gövde. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Decision detail retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Decision not found")
    @GetMapping(ApiEndpoints.AdminDecisions.BY_ID)
    ResponseEntity<ApiStandardResponse<DecisionRecordResponse>> getDecisionDetail(
            @Parameter(description = "Karar kaydının id'si") @PathVariable UUID decisionId,
            @Parameter(description = "Kaydın hangi tabloda aranacağını belirler (MANUAL / AI_*)") @RequestParam DecisionType type
    );
}
