package com.akademi.finsight.notification.mail;

import com.akademi.finsight.notification.model.RenderedNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** Tek gonderim kanali; mail paketi service'i geri cagirmadigi icin paket dongusu olusmaz. */
@Component
public class EmailNotificationSender {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailNotificationSender(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void send(RenderedNotification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(notification.getDestination());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getBody(), true);
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new EmailSendingException("E-posta gonderilemedi", exception);
        }
    }
}
