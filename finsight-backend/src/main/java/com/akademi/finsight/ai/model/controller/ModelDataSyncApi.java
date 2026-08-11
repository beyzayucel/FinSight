package com.akademi.finsight.ai.model.controller;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.ai.model.dto.response.ModelDataSyncResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(ApiEndpoints.ModelData.BASE)
@Tag(
        name = "Model Data Sync",
        description = "Pulls macro market indicators (USD, Gold, Brent, US10Y, Inflation, Policy Rate) and fund price data from Infina into the database"
)
public interface ModelDataSyncApi {

    @Operation(
            summary = "Sync AI model input data from Infina",
            description = """
                Fetches macro indicators (FX, Gold, Brent, Bond, Inflation)
                and fund price metrics (active value, portfolio value, cash,
                investor count) from Infina.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Model data synced successfully"
    )
    @ApiResponse(
            responseCode = "502",
            description = "Infina returned an error or incomplete response"
    )
    @ApiResponse(
            responseCode = "503",
            description = "Infina service is currently unavailable"
    )
    @PostMapping(ApiEndpoints.ModelData.SYNC)
    ResponseEntity<ApiStandardResponse<ModelDataSyncResponse>> sync();
}
