package tuum.example.tuum.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateAccountRequest {
    private Long customerId;
    private String country;
    private List<String> currencies;
}
