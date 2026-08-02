package com.akademi.finsight.notification.service.impl;

import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;
import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.EmailService;
import com.akademi.finsight.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final NotificationService notificationService;

    /** request.verificationUrl() zaten cagiran tarafindan kurulmus tam bir URL'dir, oldugu gibi kullanilir. */
    @Override
    public void sendVerificationEmail(VerificationTokenRequest request, Locale locale) {
        Map<String, String> params = new HashMap<>();
        params.put("username", request.username());
        params.put("temporaryPassword", request.temporaryPassword());
        params.put("verificationUrl", request.verificationUrl());

        notificationService.notify(new NotificationCommand(
                NotificationType.VERIFICATION_EMAIL,
                request.email(),
                params,
                locale == null ? null : locale.getLanguage()
        ));
    }
}
