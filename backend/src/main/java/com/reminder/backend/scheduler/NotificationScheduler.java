package com.reminder.backend.scheduler;

import com.reminder.backend.models.Event;
import com.reminder.backend.models.User;
import com.reminder.backend.repositories.EventRepository;
import com.reminder.backend.repositories.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void checkAndSendReminders() {

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        logger.info("Social Scheduler running for: {}/{}", month, day);

        List<Event> todaysCelebrations = eventRepository.findByMonthAndDay(month, day);

        List<User> community = userRepository.findAll();

        if (todaysCelebrations.isEmpty()) {
            logger.info("No celebrations today.");
            return;
        }

        for (Event event : todaysCelebrations) {
            User celebrant = event.getUser();

            if (celebrant == null) continue;
            String eventName = resolveEventName(event);
            EmailTemplate template = buildTemplate(event, celebrant, eventName);
            String celebrantEmail = celebrant.getEmail();

            for (User friend : community) {
                sendEmail(friend.getEmail(), celebrantEmail, template.subject(), template.htmlBody());
            }
        }
    }

    private String resolveEventName(Event event) { //for config errors
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            return event.getDescription().trim();
        }
        if (event.getEventType() != null && !event.getEventType().isBlank()) {
            return event.getEventType().trim();
        }
        return "Celebration";
    }

    private EmailTemplate buildTemplate(Event event, User celebrant, String eventName) {
        String type = event.getEventType() == null ? "" : event.getEventType().trim().toLowerCase(Locale.ROOT);
        String celebrantName = safe(celebrant.getUsername());
        String eventLabel = safe(eventName);

        if (type.contains("birthday")) {
            return new EmailTemplate(
                    "Birthday Celebration: " + eventLabel,
                    """
                    <html><body style='margin:0;padding:0;background:#fff8e8;font-family:Arial,sans-serif;color:#2d2d2d;'>
                      <div style='max-width:640px;margin:24px auto;background:#ffffff;border:1px solid #ffe3a6;border-radius:12px;overflow:hidden;'>
                        <img src='https://images.unsplash.com/photo-1464349153735-7db50ed83c84?auto=format&fit=crop&w=1200&q=80' alt='Birthday celebration' style='width:100%%;height:220px;object-fit:cover;' />
                        <div style='padding:24px;'>
                          <h2 style='margin:0 0 12px 0;color:#b45309;'>Birthday Reminder</h2>
                          <p style='margin:0 0 12px 0;'>Today we are celebrating <strong>%s</strong>'s <strong>%s</strong>.</p>
                          <p style='margin:0 0 12px 0;'>Please send your birthday wishes and kudos.</p>
                          <p style='margin:0;'>Thanks,<br/>Reminder Team</p>
                        </div>
                      </div>
                    </body></html>
                    """.formatted(celebrantName, eventLabel)
            );
        }

        if (type.contains("anniversary")) {
            return new EmailTemplate(
                    "Anniversary Celebration: " + eventLabel,
                    """
                    <html><body style='margin:0;padding:0;background:#eef6ff;font-family:Arial,sans-serif;color:#1f2937;'>
                      <div style='max-width:640px;margin:24px auto;background:#ffffff;border:1px solid #cfe5ff;border-radius:12px;overflow:hidden;'>
                        <img src='https://images.unsplash.com/photo-1522673607200-164d1b6ce486?auto=format&fit=crop&w=1200&q=80' alt='Anniversary celebration' style='width:100%%;height:220px;object-fit:cover;' />
                        <div style='padding:24px;'>
                          <h2 style='margin:0 0 12px 0;color:#1d4ed8;'>Anniversary Reminder</h2>
                          <p style='margin:0 0 12px 0;'>Today we are celebrating <strong>%s</strong>'s <strong>%s</strong>.</p>
                          <p style='margin:0 0 12px 0;'>Please share your wishes and congratulations.</p>
                          <p style='margin:0;'>Thanks,<br/>Reminder Team</p>
                        </div>
                      </div>
                    </body></html>
                    """.formatted(celebrantName, eventLabel)
            );
        }

        return new EmailTemplate(
                "Celebration Alert: " + eventLabel,
                """
                <html><body style='margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#111827;'>
                  <div style='max-width:640px;margin:24px auto;background:#ffffff;border:1px solid #dbe3ef;border-radius:12px;overflow:hidden;'>
                    <img src='https://images.unsplash.com/photo-1513151233558-d860c5398176?auto=format&fit=crop&w=1200&q=80' alt='Celebration' style='width:100%%;height:220px;object-fit:cover;' />
                    <div style='padding:24px;'>
                      <h2 style='margin:0 0 12px 0;color:#0f766e;'>Celebration Reminder</h2>
                      <p style='margin:0 0 12px 0;'>Today we are celebrating <strong>%s</strong>'s <strong>%s</strong>.</p>
                      <p style='margin:0 0 12px 0;'>Please wish them and share your kudos.</p>
                      <p style='margin:0;'>Thanks,<br/>Reminder Team</p>
                    </div>
                  </div>
                </body></html>
                """.formatted(celebrantName, eventLabel)
        );
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void sendEmail(String to, String cc, String subject, String body) {
        if (to == null || to.isEmpty()) return;
        if (mailFrom == null || mailFrom.isBlank()) {
            logger.warn("Mail sender is not configured; skipping email to {}", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8"); //formatting and adding fields
            helper.setFrom(mailFrom);
            helper.setTo(to);
            if (cc != null && !cc.isBlank() && !cc.equalsIgnoreCase(to)) {
                helper.setCc(cc);
            }
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            logger.info("Mail successfully sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send mail", e);
        }
    }

    private record EmailTemplate(String subject, String htmlBody) {}
}
