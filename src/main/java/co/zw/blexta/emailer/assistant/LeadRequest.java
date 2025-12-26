package co.zw.blexta.emailer.assistant;

import lombok.Data;

@Data
public class LeadRequest {

    private String session_id;

    private String name;
    private String email;
    private String phone;

    private String project_description;
    private String project_type;
    private String budget_range;
    private String timeline;
}
