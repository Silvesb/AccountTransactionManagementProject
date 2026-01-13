package tuum.example.tuum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tuum.example.tuum.model.CreateTransactionRequest;
import tuum.example.tuum.model.TransactionResponse;
import tuum.example.tuum.service.TransactionService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    // http://localhost:8080/accounts/1/transactions
    @PostMapping("accounts/{accountId}/transactions")
    public TransactionResponse createTransaction(@PathVariable Long accountId, @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(accountId, request);
    }

    // http://localhost:8080/accounts/1/transactions
    @GetMapping("accounts/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactions(accountId);
    }
}
