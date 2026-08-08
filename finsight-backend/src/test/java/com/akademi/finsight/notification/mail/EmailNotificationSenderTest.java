package com.akademi.finsight.notification.mail;

import com.akademi.finsight.notification.exception.EmailSendingException;
import com.akademi.finsight.notification.model.RenderedNotification;
import com.akademi.finsight.notification.support.NotificationFixtures;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    private static final String FROM = "noreply@finsight.local";

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private EmailNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new EmailNotificationSender(mailSender, new MailProperties(FROM));
    }

    // ──────────────────────────────────────────────────────────────
    // send
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("should map sender, recipient, subject and HTML body onto the message")
        void send_shouldBuildMimeMessageFromRenderedNotification() throws Exception {
            stubEmptyMimeMessage();
            RenderedNotification notification = new RenderedNotification(
                    NotificationFixtures.EMAIL, "Finsight - Hos geldiniz", "<h1>Merhaba</h1>");

            sender.send(notification);

            verify(mailSender).send(messageCaptor.capture());
            MimeMessage message = messageCaptor.getValue();
            assertEquals(FROM, message.getFrom()[0].toString());
            assertEquals(NotificationFixtures.EMAIL, message.getAllRecipients()[0].toString());
            assertEquals("Finsight - Hos geldiniz", message.getSubject());
            // Content-Type basligi ancak saveChanges() ile yazilir, bu yuzden DataHandler uzerinden bakilir
            assertTrue(message.getDataHandler().getContentType().contains("text/html"));
            assertEquals("<h1>Merhaba</h1>", message.getContent());
        }

        @Test
        @DisplayName("should wrap SMTP failure in EmailSendingException")
        void send_shouldWrapMailException() {
            stubEmptyMimeMessage();
            doThrow(new MailSendException("smtp erisilemez")).when(mailSender).send(any(MimeMessage.class));
            RenderedNotification notification = new RenderedNotification(
                    NotificationFixtures.EMAIL, "konu", "govde");

            EmailSendingException exception = assertThrows(
                    EmailSendingException.class, () -> sender.send(notification));

            assertInstanceOf(MailSendException.class, exception.getCause());
        }

        @Test
        @DisplayName("should wrap invalid recipient address in EmailSendingException")
        void send_shouldWrapMessagingExceptionForInvalidAddress() {
            stubEmptyMimeMessage();
            RenderedNotification notification = new RenderedNotification("gecersiz adres", "konu", "govde");

            assertThrows(EmailSendingException.class, () -> sender.send(notification));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private void stubEmptyMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }
}
