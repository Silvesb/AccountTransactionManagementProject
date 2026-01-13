package tuum.example.tuum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceEventPayload {
    private Long accountId;
    private String currency;
    private BigDecimal availableAmount;
}
