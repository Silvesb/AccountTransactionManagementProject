package tuum.example.tuum.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
}
