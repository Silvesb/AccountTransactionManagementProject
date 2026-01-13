package tuum.example.tuum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountEventPayload {
    private Long accountId;
    private Long customerId;
    private String country;
}
