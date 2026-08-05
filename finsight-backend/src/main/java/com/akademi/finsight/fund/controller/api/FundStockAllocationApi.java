package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.dto.request.FundStockAllocationRequest;
import com.akademi.finsight.fund.dto.response.FundStockAllocationResponse;
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

@RequestMapping(ApiEndpoints.FundStockAllocations.BASE)
@Tag(
        name = "Fund Stock Allocation Management",
        description = "CRUD operations for fund stock allocations"
)
public interface FundStockAllocationApi {

    @Operation(summary = "Create fund stock allocation",
            description = "Creates a new stock allocation entry for a fund.")
    @ApiResponse(responseCode = "201", description = "Fund stock allocation created successfully")
    @PostMapping
    ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> create(
            @Valid @RequestBody FundStockAllocationRequest request);

    @Operation(summary = "Get fund stock allocation by id",
            description = "Returns a single fund stock allocation by its id.")
    @ApiResponse(responseCode = "200", description = "Fund stock allocation retrieved successfully")
    @GetMapping(ApiEndpoints.FundStockAllocations.BY_ID)
    ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> getById(@PathVariable UUID id);

    @Operation(summary = "List fund stock allocations",
            description = "Returns a paginated list of fund stock allocations.")
    @ApiResponse(responseCode = "200", description = "Fund stock allocations retrieved successfully")
    @GetMapping
    ResponseEntity<ApiStandardResponse<PageResponse<FundStockAllocationResponse>>> getAll(
            @ParameterObject Pageable pageable);

    @Operation(summary = "Get stock allocations of a fund for a period",
            description = "Returns the stock allocation entries of a fund (by its code) for the given disclosure period, ordered by weight.")
    @ApiResponse(responseCode = "200", description = "Fund stock allocations retrieved successfully")
    @GetMapping(ApiEndpoints.FundStockAllocations.BY_FUND_AND_PERIOD)
    ResponseEntity<ApiStandardResponse<List<FundStockAllocationResponse>>> getByFundCodeAndPeriod(
            @PathVariable String fundCode,
            @PathVariable String period);

    @Operation(summary = "Update fund stock allocation",
            description = "Updates an existing fund stock allocation.")
    @ApiResponse(responseCode = "200", description = "Fund stock allocation updated successfully")
    @PutMapping(ApiEndpoints.FundStockAllocations.BY_ID)
    ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FundStockAllocationRequest request);

    @Operation(summary = "Delete fund stock allocation",
            description = "Deletes a fund stock allocation by its id.")
    @ApiResponse(responseCode = "204", description = "Fund stock allocation deleted successfully")
    @DeleteMapping(ApiEndpoints.FundStockAllocations.BY_ID)
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
