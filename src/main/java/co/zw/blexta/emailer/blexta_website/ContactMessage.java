package co.zw.blexta.emailer.blexta_website;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {

    private String fullName;
    private String email;
    private String solution;
    private String requirements;
}
