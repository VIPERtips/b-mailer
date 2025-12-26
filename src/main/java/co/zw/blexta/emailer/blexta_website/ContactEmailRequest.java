package co.zw.blexta.emailer.blexta_website;

import lombok.Data;

@Data
public class ContactEmailRequest {

    private ContactMessage message;
    private String timestamp;
}
