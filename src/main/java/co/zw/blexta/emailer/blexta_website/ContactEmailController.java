package co.zw.blexta.emailer.blexta_website;

import co.zw.blexta.emailer.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(
        name = "Blexta Contact Form",
        description = "Handles contact form submissions from the Blexta website"
)
public class ContactEmailController {

    private final ContactEmailService contactEmailService;

    @Operation(
            summary = "Send contact form email",
            description = "Sends a contact request email submitted from the Blexta website contact form"
    )
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendContactEmail(@RequestBody ContactEmailRequest request) {

        if (request.getMessage() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Contact message is missing")
                            .build()
            );
        }

        ContactMessage msg = request.getMessage();

        if (msg.getFullName() == null || msg.getFullName().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Full name is required")
                            .build()
            );
        }

        if (msg.getEmail() == null || msg.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Email address is required")
                            .build()
            );
        }

        request.setTimestamp(java.time.ZonedDateTime.now().toString());
        contactEmailService.sendContactNotification(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Contact email sent successfully")
                        .build()
        );
    }
}