package com.akademi.finsight.fund.chat.controller.api;

import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.chat.constant.FundChatEndpoints;
import com.akademi.finsight.fund.chat.dto.request.FundChatRequest;
import com.akademi.finsight.fund.chat.dto.response.FundChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(FundChatEndpoints.BASE)
@Tag(
        name = "Fund Dashboard Chat",
        description = "Conversational assistant for the fund dashboard screen, grounded in the dashboard payload"
)
public interface FundChatApi {

    @Operation(
            summary = "Ask the fund dashboard assistant",
            description = """
                    Answers a question about the fund using the same payload the dashboard shows, plus the
                    editable content under resources/ai/fund-chat. Independent of the ONNX recommendation
                    flow. Omit sessionId to start a conversation; reuse the returned id to continue it.
                    History lives in Redis and expires after the configured idle window.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer produced"),
            @ApiResponse(responseCode = "400", description = "Invalid message or session id"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Fund not found or not synced yet"),
            @ApiResponse(responseCode = "502", description = "Answer provider failed")
    })
    @PostMapping(FundChatEndpoints.CHAT)
    ResponseEntity<ApiStandardResponse<FundChatResponse>> ask(
            @Parameter(description = "Fund code", example = "TIE")
            @PathVariable String fundCode,
            @Valid @RequestBody FundChatRequest request,
            @AuthenticationPrincipal String email);

    @Operation(
            summary = "Clear a chat session",
            description = "Drops the stored conversation so the next question starts from scratch.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session cleared"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping(FundChatEndpoints.CHAT_SESSION)
    ResponseEntity<Void> reset(
            @Parameter(description = "Fund code", example = "TIE")
            @PathVariable String fundCode,
            @Parameter(description = "Session to clear")
            @PathVariable String sessionId,
            @AuthenticationPrincipal String email);
}
