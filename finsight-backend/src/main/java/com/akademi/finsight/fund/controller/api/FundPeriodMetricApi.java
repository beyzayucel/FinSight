package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RequestMapping(ApiEndpoints.FundPeriodMetrics.BASE)
@Tag(
        name = "Fund Period Metric Management",
        description = "CRUD operations for fund period metrics"
)
public interface FundPeriodMetricApi {

    @Operation(summary = "Create fund period metric",
            description = "Creates a new period metric entry for a fund.")
    @ApiResponse(responseCode = "201", description = "Fund period metric created successfully")
    @PostMapping
    ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> create(
            @Valid @RequestBody FundPeriodMetricRequest request);

    @Operation(summary = "Get fund period metric by id",
            description = "Returns a single fund period metric by its id.")
    @ApiResponse(responseCode = "200", description = "Fund period metric retrieved successfully")
    @GetMapping(ApiEndpoints.FundPeriodMetrics.BY_ID)
    ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> getById(@PathVariable UUID id);

    @Operation(summary = "List fund period metrics",
            description = "Returns a paginated list of fund period metrics.")
    @ApiResponse(responseCode = "200", description = "Fund period metrics retrieved successfully")
    @GetMapping
    ResponseEntity<ApiStandardResponse<PageResponse<FundPeriodMetricResponse>>> getAll(
            @ParameterObject Pageable pageable);

    @Operation(summary = "Get latest period metrics of a fund",
            description = "Returns the period metrics of a fund (by its code) for its most recent data date.")
    @ApiResponse(responseCode = "200", description = "Latest fund period metrics retrieved successfully")
    @GetMapping(ApiEndpoints.FundPeriodMetrics.LATEST_BY_FUND)
    ResponseEntity<ApiStandardResponse<List<FundPeriodMetricResponse>>> getLatestByFundCode(
            @PathVariable String fundCode);

    @Operation(summary = "Update fund period metric",
            description = "Updates an existing fund period metric.")
    @ApiResponse(responseCode = "200", description = "Fund period metric updated successfully")
    @PutMapping(ApiEndpoints.FundPeriodMetrics.BY_ID)
    ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FundPeriodMetricRequest request);

    @Operation(summary = "Delete fund period metric",
            description = "Deletes a fund period metric by its id.")
    @ApiResponse(responseCode = "204", description = "Fund period metric deleted successfully")
    @DeleteMapping(ApiEndpoints.FundPeriodMetrics.BY_ID)
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
