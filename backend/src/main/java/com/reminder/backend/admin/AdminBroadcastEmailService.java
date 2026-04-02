package com.reminder.backend.admin;

import com.reminder.backend.models.User;
import com.reminder.backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AdminBroadcastEmailService {
    private static final Logger logger = LoggerFactory.getLogger(AdminBroadcastEmailService.class);

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AdminBroadcastEmailService(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public AdminBroadcastEmailResult broadcast(String subject, String body) {
        Set<String> recipients = new LinkedHashSet<>();//faster lookup, maintains order of insertion and no duplicates
        for (User user : userRepository.findAll()) {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                recipients.add(user.getEmail().trim().toLowerCase());
            }
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            logger.warn("Mail sender is not configured; skipping broadcast");
            return new AdminBroadcastEmailResult(recipients.size(), 0, recipients.size());
        }

        int sent = 0;
        int failed = 0;
        for (String recipient : recipients) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailFrom);
                message.setTo(recipient);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                sent++;
            } catch (Exception ex) {
                failed++;
                logger.error("Failed to send broadcast mail to {}", recipient, ex);
            }
        }

        return new AdminBroadcastEmailResult(recipients.size(), sent, failed);
    }
}
