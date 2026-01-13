package tuum.example.tuum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tuum.example.tuum.model.AccountResponse;
import tuum.example.tuum.model.CreateAccountRequest;
import tuum.example.tuum.service.AccountService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    @Autowired
    AccountService accountService;

    // http://localhost:8080/accounts
    @PostMapping("accounts")
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    // http://localhost:8080/accounts/1
    @GetMapping("accounts/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId) {
        return accountService.getAccount(accountId);
    }

}
