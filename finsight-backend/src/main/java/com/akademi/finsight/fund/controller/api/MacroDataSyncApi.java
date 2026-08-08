package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.entity.MacroData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(ApiEndpoints.MacroData.BASE)
@Tag(
        name = "Macro Data Sync",
        description = "Pulls macroeconomic and market indicator data (USD, Gold, Brent, US10Y, Inflation) from Infina into the database"
)
public interface MacroDataSyncApi {

    @Operation(summary = "Sync macroeconomic data from Infina",
            description = """
                    Fetches market prices (FX, Gold, Brent, Bond) and inflation data from Infina,
                    computes daily returns, and saves them into the macro_data table.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Macro data synced successfully"),
            @ApiResponse(responseCode = "502", description = "Infina returned an error or incomplete response"),
            @ApiResponse(responseCode = "503", description = "Infina service is currently unavailable")
    })
    @PostMapping(ApiEndpoints.MacroData.SYNC)
    ResponseEntity<ApiStandardResponse<MacroData>> sync();
}
