package com.akademi.finsight.notification.mail;

import com.akademi.finsight.notification.exception.EmailSendingException;
import com.akademi.finsight.notification.model.RenderedNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** Tek gonderim kanali; mail paketi service'i geri cagirmadigi icin paket dongusu olusmaz. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender {

    private static final String LOGO_CONTENT_ID = "logo";
    private static final String LOGO_CLASSPATH = "static/images/logo.png";

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void send(RenderedNotification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart=true olmadan addInline() ile gomulu resim (cid:) kullanilamaz.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(notification.getDestination());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getBody(), true);
            attachLogo(helper);
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new EmailSendingException(exception);
        }
    }

    private void attachLogo(MimeMessageHelper helper) {
        try {
            helper.addInline(LOGO_CONTENT_ID, new ClassPathResource(LOGO_CLASSPATH));
        } catch (MessagingException exception) {
            log.warn("Failed to attach logo to email, continuing without it. Reason: {}", exception.getMessage());
        }
    }
}
