package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
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

@RequestMapping(ApiEndpoints.FundDistributions.BASE)
@Tag(
        name = "Fund Distribution Management",
        description = "CRUD operations for fund distributions"
)
public interface FundDistributionApi {

    @Operation(summary = "Create fund distribution",
            description = "Creates a new distribution entry for a fund.")
    @ApiResponse(responseCode = "201", description = "Fund distribution created successfully")
    @PostMapping
    ResponseEntity<ApiStandardResponse<FundDistributionResponse>> create(
            @Valid @RequestBody FundDistributionRequest request);

    @Operation(summary = "Get fund distribution by id",
            description = "Returns a single fund distribution by its id.")
    @ApiResponse(responseCode = "200", description = "Fund distribution retrieved successfully")
    @GetMapping(ApiEndpoints.FundDistributions.BY_ID)
    ResponseEntity<ApiStandardResponse<FundDistributionResponse>> getById(@PathVariable UUID id);

    @Operation(summary = "List fund distributions",
            description = "Returns a paginated list of fund distributions.")
    @ApiResponse(responseCode = "200", description = "Fund distributions retrieved successfully")
    @GetMapping
    ResponseEntity<ApiStandardResponse<PageResponse<FundDistributionResponse>>> getAll(
            @ParameterObject Pageable pageable);

    @Operation(summary = "Get latest distribution of a fund",
            description = "Returns the distribution entries of a fund (by its code) for its most recent distribution date.")
    @ApiResponse(responseCode = "200", description = "Latest fund distribution retrieved successfully")
    @GetMapping(ApiEndpoints.FundDistributions.LATEST_BY_FUND)
    ResponseEntity<ApiStandardResponse<List<FundDistributionResponse>>> getLatestByFundCode(
            @PathVariable String fundCode);

    @Operation(summary = "Update fund distribution",
            description = "Updates an existing fund distribution.")
    @ApiResponse(responseCode = "200", description = "Fund distribution updated successfully")
    @PutMapping(ApiEndpoints.FundDistributions.BY_ID)
    ResponseEntity<ApiStandardResponse<FundDistributionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FundDistributionRequest request);

    @Operation(summary = "Delete fund distribution",
            description = "Deletes a fund distribution by its id.")
    @ApiResponse(responseCode = "204", description = "Fund distribution deleted successfully")
    @DeleteMapping(ApiEndpoints.FundDistributions.BY_ID)
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
