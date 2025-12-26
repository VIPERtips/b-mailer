package co.zw.blexta.emailer.assistant;

import co.zw.blexta.emailer.assistant.dto.LeadNotificationRequest;
import co.zw.blexta.emailer.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emailer")
@RequiredArgsConstructor
@Tag(name = "leads", description = "Endpoints for sending email notifications for leads")
public class LeadController {

    private final EmailService emailService;

    @Operation(
            summary = "Send lead notification email",
            description = "Triggers an email notification when a new lead is submitted"
    )
    @PostMapping("/notify")
    public ResponseEntity<ApiResponse<Void>> notifyLead(
            @RequestBody LeadNotificationRequest request
    ) {

        if (request.getLead() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lead data is missing")
                            .build()
            );
        }

        request.setTimestamp(java.time.ZonedDateTime.now().toString());

        emailService.sendLeadNotification(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Lead notification sent")
                        .build()
        );
    }
}
