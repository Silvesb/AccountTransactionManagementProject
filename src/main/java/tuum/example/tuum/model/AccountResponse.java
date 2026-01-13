package tuum.example.tuum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountResponse {
    private Long accountId;
    private Long customerId;
    private List<BalanceResponse> balances;
}
