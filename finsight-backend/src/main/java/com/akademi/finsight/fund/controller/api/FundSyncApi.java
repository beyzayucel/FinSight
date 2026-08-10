package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.dto.response.FundSyncResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(
        name = "Fund Data Sync",
        description = "Pulls the tracked fund's data from Infina into the database"
)
public interface FundSyncApi {

    @Operation(
            summary = "Sync fund data from Infina",
            description = """
                Fetches the tracked fund (configured via 'fund.code', TIE by default)
                from Infina and upserts its period metrics, asset distribution, and
                stock allocations. Running it twice for the same data date overwrites
                the existing rows instead of duplicating them.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fund data synced successfully"
    )
    @ApiResponse(
            responseCode = "502",
            description = "Infina returned an error or incomplete response"
    )
    @ApiResponse(
            responseCode = "503",
            description = "Infina service is currently unavailable"
    )
    @PostMapping(ApiEndpoints.Funds.SYNC)
    ResponseEntity<ApiStandardResponse<FundSyncResponse>> sync();
}
