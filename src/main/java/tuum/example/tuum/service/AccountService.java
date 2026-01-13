package tuum.example.tuum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuum.example.tuum.entity.Account;
import tuum.example.tuum.entity.Balance;
import tuum.example.tuum.model.AccountEventPayload;
import tuum.example.tuum.model.AccountResponse;
import tuum.example.tuum.model.BalanceEventPayload;
import tuum.example.tuum.model.BalanceResponse;
import tuum.example.tuum.model.CreateAccountRequest;
import tuum.example.tuum.repository.AccountRepository;
import tuum.example.tuum.repository.BalanceRepository;
import tuum.example.tuum.util.CurrencyType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    AccountRepository accountRepo;

    @Autowired
    BalanceRepository balRepo;

    @Autowired
    EventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request.getCustomerId() == null) {
            throw new RuntimeException("Customer ID is empty");
        }
        if (request.getCountry() == null || request.getCountry().isBlank()) {
            throw new RuntimeException("Country is empty");
        }
        if (request.getCurrencies() == null || request.getCurrencies().isEmpty()) {
            throw new RuntimeException("No currencies provided");
        }

        Set<String> uniqueCurrencies = new LinkedHashSet<>(request.getCurrencies());
        if (uniqueCurrencies.size() != request.getCurrencies().size()) {
            throw new RuntimeException("Duplicate currencies");
        }

        Account acct = new Account();
        acct.setCustomerId(request.getCustomerId());
        acct.setCountry(request.getCountry());
        accountRepo.insert(acct);
        if (acct.getId() == null) {
            throw new RuntimeException("Account ID not generated");
        }

        eventPublisher.send("INSERT", "ACCOUNT",
                new AccountEventPayload(acct.getId(), acct.getCustomerId(), acct.getCountry()));

        // dedupe currency list
        List<Balance> balancesForAccount = new ArrayList<>();
        List<BalanceResponse> balances = new ArrayList<>();
        for (String currencyValue : uniqueCurrencies) {
            CurrencyType currencyType = CurrencyType.fromString(currencyValue);
            Balance bal = insertBalance(acct.getId(), currencyType);
            balancesForAccount.add(bal);
            balances.add(new BalanceResponse(bal.getAvailableAmount(), bal.getCurrency()));
        }
        acct.setBalances(balancesForAccount);
        log.info("account created id={}, balances={}", acct.getId(), balancesForAccount.size());

        return new AccountResponse(acct.getId(), acct.getCustomerId(), balances);
    }

    public AccountResponse getAccount(Long accountId) {
        Account acct = accountRepo.findById(accountId);
        if (acct == null) {
            throw new RuntimeException("No account with this ID.");
        }
        List<Balance> balancesForAccount = balRepo.findByAccountId(accountId);
        acct.setBalances(balancesForAccount);
        List<BalanceResponse> balances = balancesForAccount.stream()
                .map(bal -> new BalanceResponse(bal.getAvailableAmount(), bal.getCurrency()))
                .toList();
        return new AccountResponse(acct.getId(), acct.getCustomerId(), balances);
    }

    private Balance insertBalance(Long accountId, CurrencyType currencyType) {
        Balance bal = new Balance();
        bal.setAccountId(accountId);
        bal.setCurrency(currencyType.name());
        bal.setAvailableAmount(BigDecimal.ZERO);
        balRepo.insert(bal);

        eventPublisher.send("INSERT", "BALANCE",
                new BalanceEventPayload(bal.getAccountId(), bal.getCurrency(), bal.getAvailableAmount()));

        return bal;
    }
}
