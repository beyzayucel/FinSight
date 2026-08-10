package com.akademi.finsight.fund.chat.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.chat.controller.api.FundChatApi;
import com.akademi.finsight.fund.chat.dto.request.FundChatRequest;
import com.akademi.finsight.fund.chat.dto.response.FundChatResponse;
import com.akademi.finsight.fund.chat.service.FundChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FundChatController extends BaseController implements FundChatApi {

    private final FundChatService fundChatService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundChatResponse>> ask(String fundCode,
                                                                    FundChatRequest request,
                                                                    String email) {
        return ok(fundChatService.ask(email, fundCode, request));
    }

    @Override
    public ResponseEntity<Void> reset(String fundCode, String sessionId, String email) {
        fundChatService.reset(email, fundCode, sessionId);
        return noContent();
    }
}
