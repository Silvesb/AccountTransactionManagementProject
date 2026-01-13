package tuum.example.tuum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceResponse {
    private BigDecimal availableAmount;
    private String currency;
}
