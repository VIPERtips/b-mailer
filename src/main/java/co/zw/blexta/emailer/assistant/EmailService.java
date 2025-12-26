package co.zw.blexta.emailer.assistant;

import co.zw.blexta.emailer.assistant.dto.LeadNotificationRequest;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final String FROM_EMAIL = "support@blexta.co.zw";
    private static final String FROM_NAME = "Blexta Assistant";
    private static final String BASE_URL = "https://blexta.co.zw";

    @Autowired
    private JavaMailSender mailSender;

    private String cachedTemplate;

    @Async("notificationExecutor")
    public void sendLeadNotification(LeadNotificationRequest request) {
        LeadRequest lead = request.getLead();

        EmailBuilder builder = new EmailBuilder()
                .to("tadiwachipungu2@gmail.com")
                .subject("New Lead Received via Blexta Assistant")
                .greeting("Hello Blexta Team,")
                .message("A new lead has been submitted through the Blexta Assistant.")
                .leadBlock(
                        lead.getSession_id(),
                        lead.getName(),
                        lead.getEmail(),
                        lead.getPhone(),
                        lead.getProject_type(),
                        lead.getProject_description(),
                        lead.getBudget_range(),
                        lead.getTimeline(),
                        request.getTimestamp()
                )
                .cta("View Dashboard", "https://blexta-assistant.vercel.app" + "/admin")
                .footerMessage("Blexta • Ideas . Wired . Different");

        dispatchEmail(builder);
    }

    private void dispatchEmail(EmailBuilder builder) {
        try {
            String template = getTemplate();
            String processedHtml = fillTemplate(template, builder);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            helper.setTo(builder.toEmail);
            helper.setSubject(builder.subject);
            helper.setText(processedHtml, true);

            mailSender.send(message);
            System.out.println("Lead notification email sent to " + builder.toEmail);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to send lead email", ex);
        }
    }

    private String getTemplate() {
        if (cachedTemplate == null) {
            try (var stream = getClass().getClassLoader()
                    .getResourceAsStream("templates/blexta-lead.html")) {

                if (stream == null)
                    throw new RuntimeException("Email template not found");

                cachedTemplate = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load email template", e);
            }
        }
        return cachedTemplate;
    }

    private String fillTemplate(String template, EmailBuilder builder) {
        return template
                .replace("{{EMAIL_TITLE}}", n(builder.subject))
                .replace("{{GREETING}}", n(builder.greeting))
                .replace("{{MESSAGE_CONTENT}}", n(builder.message))
                .replace("{{LEAD_BLOCK}}", builder.leadBlock != null ? builder.leadBlock : "")
                .replace("{{CTA_TEXT}}", n(builder.ctaText))
                .replace("{{CTA_URL}}", n(builder.ctaUrl))
                .replace("{{FOOTER_MESSAGE}}", n(builder.footerMessage))
                .replace("{{ICON_URL}}", BASE_URL + "/favicon.ico");
    }

    private String n(String value) {
        return value == null ? "" : value;
    }

    public static class EmailBuilder {

        private String toEmail;
        private String subject;
        private String greeting;
        private String message;
        private String ctaText;
        private String ctaUrl;
        private String footerMessage;
        private String leadBlock;

        public EmailBuilder to(String email) {
            this.toEmail = email;
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

        public EmailBuilder cta(String text, String url) {
            this.ctaText = text;
            this.ctaUrl = url;
            return this;
        }

        public EmailBuilder footerMessage(String footer) {
            this.footerMessage = footer;
            return this;
        }

        public EmailBuilder leadBlock(
                String sessionId,
                String name,
                String email,
                String phone,
                String projectType,
                String description,
                String budget,
                String timeline,
                String timestamp
        ) {
            this.leadBlock =
                    "<div class=\"lead-block\">" +
                            "<p><strong>Session ID:</strong> " + sessionId + "</p>" +
                            "<p><strong>Name:</strong> " + name + "</p>" +
                            "<p><strong>Email:</strong> " + email + "</p>" +
                            "<p><strong>Phone:</strong> " + phone + "</p>" +
                            "<p><strong>Project Type:</strong> " + projectType+ "</p>" +
                            "<p><strong>Description:</strong> " + description + "</p>" +
                            "<p><strong>Budget:</strong> " + budget + "</p>" +
                            "<p><strong>Timeline:</strong> " + timeline + "</p>" +
                            "<p><strong>Submitted At:</strong> " + timestamp + "</p>" +
                            "</div>";
            return this;
        }
    }
}
