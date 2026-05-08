package co.zw.blexta.emailer.blexta_website;

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
public class ContactEmailService {

    private static final String FROM_EMAIL = "support@blexta.co.zw";
    private static final String FROM_NAME = "Blexta";
    private static final String BASE_URL = "https://blexta.co.zw";

    @Autowired
    private JavaMailSender mailSender;

    private String cachedTemplate;

    @Async("notificationExecutor")
    public void sendContactNotification(ContactEmailRequest request) {

        ContactMessage msg = request.getMessage();

        EmailBuilder builder = new EmailBuilder()
                .to("tadiwachipungu2@gmail.com")
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
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            helper.setTo(builder.toEmail);
            helper.setSubject(builder.subject);
            helper.setText(processedHtml, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + builder.toEmail + " (CC: hello@blexta.co.zw)");

        } catch (Exception ex) {
            throw new RuntimeException("Failed to send contact email", ex);
        }
    }


    private String getTemplate() {
        if (cachedTemplate == null) {
            try (var stream = getClass().getClassLoader()
                    .getResourceAsStream("templates/blexta-contact.html")) {

                if (stream == null)
                    throw new RuntimeException("Contact email template not found");

                cachedTemplate =
                        new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load contact email template", e);
            }
        }
        return cachedTemplate;
    }

    private String fillTemplate(String template, EmailBuilder builder) {
        return template
                .replace("{{EMAIL_TITLE}}", n(builder.subject))
                .replace("{{GREETING}}", n(builder.greeting))
                .replace("{{MESSAGE_CONTENT}}", n(builder.message))
                .replace("{{CONTACT_BLOCK}}", builder.contactBlock)
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
        private String footerMessage;
        private String contactBlock;

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
                            "<p><strong>Name:</strong> " + fullName + "</p>" +
                            "<p><strong>Email:</strong> " + email + "</p>" +
                            "<p><strong>Selected Solution:</strong> " + solution + "</p>" +
                            "<p><strong>Requirements:</strong> " + requirements + "</p>" +
                            "<p><strong>Submitted At:</strong> " + timestamp + "</p>" +
                            "</div>";
            return this;
        }
    }
}
