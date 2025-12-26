package co.zw.blexta.emailer.assistant.dto;

import co.zw.blexta.emailer.assistant.LeadRequest;
import lombok.Data;

@Data
public class LeadNotificationRequest {

    private LeadRequest lead;
    private String notificationEmail;
    private String timestamp;
}
