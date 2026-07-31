package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.config.NotificationProperties;
import com.akademi.finsight.notification.exception.InvalidNotificationException;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.RenderedNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Olayi gonderilebilir metne donusturur. Sablon anahtari: notification.<tip>.<subject|body> */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRenderer {

    private static final Pattern UNRESOLVED_PLACEHOLDER = Pattern.compile("\\{[a-zA-Z][a-zA-Z0-9_]*}");

    private final MessageSource messageSource;
    private final NotificationProperties properties;

    public RenderedNotification render(NotificationRequestedEvent event) {
        Locale locale = Locale.forLanguageTag(event.getLocale());
        String keyPrefix = event.getType().templateKey();

        String body = interpolate(message(keyPrefix + ".body", locale), event.getParams());
        String subject = interpolate(message(keyPrefix + ".subject", locale), event.getParams());

        return new RenderedNotification(destination(event), subject, body);
    }

    private String destination(NotificationRequestedEvent event) {
        String email = event.getEmail();
        if (email == null || email.isBlank()) {
            log.error("Notification has no destination email: event={}", event.getEventId());
            throw new InvalidNotificationException();
        }
        return email;
    }

    private String message(String key, Locale locale) {
        Locale defaultLocale = Locale.forLanguageTag(properties.defaultLocale());
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException exception) {
            if (locale.equals(defaultLocale)) {
                log.error("Template not found: key={}", key);
                throw new InvalidNotificationException();
            }
            return messageSource.getMessage(key, null, defaultLocale);
        }
    }

    private String interpolate(String template, Map<String, String> params) {
        String rendered = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", escapeHtml(entry.getValue()));
        }
        if (UNRESOLVED_PLACEHOLDER.matcher(rendered).find()) {
            log.error("Template has unresolved placeholder(s) after interpolation");
            throw new InvalidNotificationException();
        }
        return rendered;
    }

    /** Kullanicidan gelen deger HTML'e kacirilmadan basilirsa enjeksiyona acik olur; "&" once kacirilmeli. */
    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
