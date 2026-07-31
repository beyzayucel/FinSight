package com.akademi.finsight.notification.service;

import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;
import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final NotificationService notificationService;

    public EmailServiceImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** request.verificationUrl() zaten cagiran tarafindan kurulmus tam bir URL'dir, oldugu gibi kullanilir. */
    @Override
    public void sendVerificationEmail(VerificationTokenRequest request, Locale locale) {
        Map<String, String> params = new HashMap<>();
        params.put("username", request.username());
        params.put("temporaryPassword", request.temporaryPassword());
        params.put("verificationUrl", request.verificationUrl());

        notificationService.notify(new NotificationCommand(
                NotificationType.VERIFICATION_EMAIL,
                null,
                request.email(),
                params,
                locale == null ? null : locale.getLanguage()
        ));
    }
}
