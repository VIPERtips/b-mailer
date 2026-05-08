package co.zw.blexta.emailer.blexta_website;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ContactEmailService {

    private static final String FROM_EMAIL = "support@blexta.co.zw";
    private static final String FROM_NAME = "Blexta";
    private static final String BASE_URL = "https://blexta.co.zw";
    private static final String NOTIFY_EMAIL = "tadiwachipungu2@gmail.com";
    private static final String CC_EMAIL = "hello@blexta.co.zw";

    @Autowired
    private JavaMailSender mailSender;

    private volatile String cachedTemplate;

    @Async("notificationExecutor")
    public void sendContactNotification(ContactEmailRequest request) {
        ContactMessage msg = request.getMessage();

        log.info("Processing contact form submission from: {}", msg.getEmail());

        EmailBuilder builder = new EmailBuilder()
                .to(NOTIFY_EMAIL)
                .cc(CC_EMAIL)
                .subject("New Contact Request from Blexta Website")
                .greeting("Hello Blexta Team,")
                .message("A new contact form submission has been received on the Blexta website.")
                .contactBlock(
                        msg.getFullName(),
                        msg.getEmail(),
                        msg.getSolution(),
                        msg.getRequirements(),
                        request.getTimestamp()
                )
                .footerMessage("Blexta • Ideas . Wired . Different");

        dispatchEmail(builder);
    }

    private void dispatchEmail(EmailBuilder builder) {
        try {
            String template = getTemplate();
            String processedHtml = fillTemplate(template, builder);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            helper.setTo(builder.toEmail);
            if (builder.ccEmail != null) {
                helper.setCc(builder.ccEmail);
            }
            helper.setSubject(builder.subject);
            helper.setText(processedHtml, true);

            mailSender.send(message);
            log.info("Contact email dispatched to: {} | CC: {}", builder.toEmail, builder.ccEmail);

        } catch (Exception ex) {
            log.error("Failed to send contact email to {}: {}", builder.toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send contact email", ex);
        }
    }

    private String getTemplate() {
        if (cachedTemplate == null) {
            synchronized (this) {
                if (cachedTemplate == null) {
                    try (var stream = getClass().getClassLoader()
                            .getResourceAsStream("templates/blexta-contact.html")) {

                        if (stream == null) {
                            throw new RuntimeException("Contact email template not found at templates/blexta-contact.html");
                        }

                        cachedTemplate = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                        log.info("Email template loaded and cached");

                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load contact email template", e);
                    }
                }
            }
        }
        return cachedTemplate;
    }

    private String fillTemplate(String template, EmailBuilder builder) {
        return template
                .replace("{{EMAIL_TITLE}}", n(builder.subject))
                .replace("{{GREETING}}", n(builder.greeting))
                .replace("{{MESSAGE_CONTENT}}", n(builder.message))
                .replace("{{CONTACT_BLOCK}}", n(builder.contactBlock))
                .replace("{{FOOTER_MESSAGE}}", n(builder.footerMessage))
                .replace("{{ICON_URL}}", BASE_URL + "/favicon.ico");
    }

    private String n(String value) {
        return value == null ? "" : value;
    }

    public static class EmailBuilder {

        private String toEmail;
        private String ccEmail;
        private String subject;
        private String greeting;
        private String message;
        private String footerMessage;
        private String contactBlock;

        public EmailBuilder to(String email) {
            this.toEmail = email;
            return this;
        }

        public EmailBuilder cc(String email) {
            this.ccEmail = email;
            return this;
        }

        public EmailBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder greeting(String greeting) {
            this.greeting = greeting;
            return this;
        }

        public EmailBuilder message(String message) {
            this.message = message;
            return this;
        }

        public EmailBuilder footerMessage(String footer) {
            this.footerMessage = footer;
            return this;
        }

        public EmailBuilder contactBlock(
                String fullName,
                String email,
                String solution,
                String requirements,
                String timestamp
        ) {
            this.contactBlock =
                    "<div class=\"contact-block\">" +
                            "<p><strong>Name:</strong> " + n(fullName) + "</p>" +
                            "<p><strong>Email:</strong> " + n(email) + "</p>" +
                            "<p><strong>Selected Solution:</strong> " + n(solution) + "</p>" +
                            "<p><strong>Requirements:</strong> " + n(requirements) + "</p>" +
                            "<p><strong>Submitted At:</strong> " + n(timestamp) + "</p>" +
                            "</div>";
            return this;
        }

        private String n(String value) {
            return value == null ? "" : value;
        }
    }
}