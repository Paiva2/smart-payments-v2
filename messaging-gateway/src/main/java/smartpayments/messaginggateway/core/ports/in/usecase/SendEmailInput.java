package smartpayments.messaginggateway.core.ports.in.usecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailInput {
    private String to;
    private String subject;
    private String template;
    private Map<CharSequence, Object> variables;
    private List<String> cc;
}
