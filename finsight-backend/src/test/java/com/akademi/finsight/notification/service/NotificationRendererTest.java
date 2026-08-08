package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.exception.InvalidNotificationException;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.model.RenderedNotification;
import com.akademi.finsight.notification.support.NotificationFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRendererTest {

    private static final Locale TR = Locale.forLanguageTag("tr");
    private static final Locale EN = Locale.forLanguageTag("en");
    private static final String SUBJECT_KEY = "notification.verificationEmail.subject";
    private static final String BODY_KEY = "notification.verificationEmail.body";

    @Mock
    private MessageSource messageSource;

    private NotificationRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new NotificationRenderer(messageSource, NotificationFixtures.properties());
    }

    // ──────────────────────────────────────────────────────────────
    // render
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("render")
    class Render {

        @Test
        @DisplayName("should fill template and take destination from event email")
        void render_shouldFillTemplateForEventLocale() {
            stubTemplates("Merhaba {username}", "Hos geldiniz");

            RenderedNotification rendered = renderer.render(NotificationFixtures.event());

            assertEquals(NotificationFixtures.EMAIL, rendered.getDestination());
            assertEquals("Hos geldiniz", rendered.getSubject());
            assertEquals("Merhaba mehmet", rendered.getBody());
        }

        /** "&" once kacirilmazsa uretilen entity'ler ikinci kez kacirilir (&lt; -> &amp;lt;). */
        @Test
        @DisplayName("should escape HTML in parameters, ampersand first")
        void render_shouldEscapeHtmlInParameters() {
            stubTemplates("<p>{username}</p>", "Konu");
            NotificationRequestedEvent event = NotificationFixtures.event(
                    NotificationType.VERIFICATION_EMAIL,
                    Map.of("username", "<script>alert(\"x\")&'"),
                    "tr");

            RenderedNotification rendered = renderer.render(event);

            assertEquals("<p>&lt;script&gt;alert(&quot;x&quot;)&amp;&#39;</p>", rendered.getBody());
        }

        @Test
        @DisplayName("should throw InvalidNotificationException when placeholder unresolved")
        void render_shouldThrowWhenPlaceholderUnresolved() {
            when(messageSource.getMessage(BODY_KEY, null, TR)).thenReturn("Gecici sifreniz: {temporaryPassword}");
            NotificationRequestedEvent event = NotificationFixtures.event(
                    NotificationType.VERIFICATION_EMAIL, Map.of("username", "mehmet"), "tr");

            assertThrows(InvalidNotificationException.class, () -> renderer.render(event));
        }

        @Test
        @DisplayName("should throw InvalidNotificationException when destination email is blank")
        void render_shouldThrowWhenDestinationBlank() {
            stubTemplates("govde", "konu");
            NotificationRequestedEvent event = NotificationFixtures.eventWithEmail("   ");

            assertThrows(InvalidNotificationException.class, () -> renderer.render(event));
        }

        @Test
        @DisplayName("should throw InvalidNotificationException when destination email is null")
        void render_shouldThrowWhenDestinationNull() {
            stubTemplates("govde", "konu");
            NotificationRequestedEvent event = NotificationFixtures.eventWithEmail(null);

            assertThrows(InvalidNotificationException.class, () -> renderer.render(event));
        }

        @Test
        @DisplayName("should fall back to default locale when template missing for event locale")
        void render_shouldFallBackToDefaultLocale() {
            when(messageSource.getMessage(BODY_KEY, null, EN)).thenThrow(new NoSuchMessageException(BODY_KEY));
            when(messageSource.getMessage(SUBJECT_KEY, null, EN)).thenThrow(new NoSuchMessageException(SUBJECT_KEY));
            stubTemplates("Turkce govde", "Turkce konu");
            NotificationRequestedEvent event = NotificationFixtures.event(
                    NotificationType.VERIFICATION_EMAIL, Map.of(), "en");

            RenderedNotification rendered = renderer.render(event);

            assertEquals("Turkce govde", rendered.getBody());
            assertEquals("Turkce konu", rendered.getSubject());
        }

        @Test
        @DisplayName("should throw InvalidNotificationException when template missing in default locale")
        void render_shouldThrowWhenTemplateMissingInDefaultLocale() {
            when(messageSource.getMessage(BODY_KEY, null, TR)).thenThrow(new NoSuchMessageException(BODY_KEY));
            NotificationRequestedEvent event = NotificationFixtures.event();

            assertThrows(InvalidNotificationException.class, () -> renderer.render(event));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private void stubTemplates(String body, String subject) {
        when(messageSource.getMessage(BODY_KEY, null, TR)).thenReturn(body);
        when(messageSource.getMessage(SUBJECT_KEY, null, TR)).thenReturn(subject);
    }
}
