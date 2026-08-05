package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(
        name = "Fund Management",
        description = "CRUD operations for funds"
)
public interface FundApi {

    @Operation(summary = "Create fund",
            description = "Creates a new fund.")
    @ApiResponse(responseCode = "201", description = "Fund created successfully")
    @PostMapping
    ResponseEntity<ApiStandardResponse<FundResponse>> create(
            @Valid @RequestBody FundRequest request);

    @Operation(summary = "Get fund by id",
            description = "Returns a single fund by its id.")
    @ApiResponse(responseCode = "200", description = "Fund retrieved successfully")
    @GetMapping(ApiEndpoints.Funds.BY_ID)
    ResponseEntity<ApiStandardResponse<FundResponse>> getById(@PathVariable UUID id);

    @Operation(summary = "List funds",
            description = "Returns all funds.")
    @ApiResponse(responseCode = "200", description = "Funds retrieved successfully")
    @GetMapping
    ResponseEntity<ApiStandardResponse<List<FundResponse>>> getAll();

    @Operation(summary = "Update fund",
            description = "Updates an existing fund.")
    @ApiResponse(responseCode = "200", description = "Fund updated successfully")
    @PutMapping(ApiEndpoints.Funds.BY_ID)
    ResponseEntity<ApiStandardResponse<FundResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FundRequest request);

    @Operation(summary = "Delete fund",
            description = "Deletes a fund by its id.")
    @ApiResponse(responseCode = "204", description = "Fund deleted successfully")
    @DeleteMapping(ApiEndpoints.Funds.BY_ID)
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
