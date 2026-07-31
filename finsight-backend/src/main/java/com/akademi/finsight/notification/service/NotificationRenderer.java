package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.config.NotificationProperties;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.RenderedNotification;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Olayi gonderilebilir metne donusturur. Sablon anahtari: notification.<tip>.<subject|body>
 */
@Component
public class NotificationRenderer {

    private static final Pattern UNRESOLVED_PLACEHOLDER = Pattern.compile("\\{[a-zA-Z][a-zA-Z0-9_]*}");

    private final MessageSource messageSource;
    private final Locale defaultLocale;

    public NotificationRenderer(MessageSource messageSource, NotificationProperties properties) {
        this.messageSource = messageSource;
        this.defaultLocale = Locale.forLanguageTag(properties.defaultLocale());
    }

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
            throw new InvalidNotificationException("E-posta adresi bulunamadi: event=" + event.getEventId());
        }
        return email;
    }

    private String message(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException exception) {
            if (locale.equals(defaultLocale)) {
                throw new InvalidNotificationException("Sablon bulunamadi: " + key);
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
            throw new InvalidNotificationException("Template parametresi eksik: " + rendered);
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
