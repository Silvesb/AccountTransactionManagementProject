package tuum.example.tuum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuum.example.tuum.entity.Account;
import tuum.example.tuum.entity.AccountTransaction;
import tuum.example.tuum.entity.Balance;
import tuum.example.tuum.model.BalanceEventPayload;
import tuum.example.tuum.model.CreateTransactionRequest;
import tuum.example.tuum.model.TransactionEventPayload;
import tuum.example.tuum.model.TransactionResponse;
import tuum.example.tuum.repository.AccountRepository;
import tuum.example.tuum.repository.BalanceRepository;
import tuum.example.tuum.repository.TransactionRepository;
import tuum.example.tuum.util.CurrencyType;
import tuum.example.tuum.util.TransactionDirection;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    AccountRepository accountRepo;

    @Autowired
    BalanceRepository balRepo;

    @Autowired
    TransactionRepository txnRepo;

    @Autowired
    EventPublisher eventPublisher;

    @Transactional
    public TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request) {
        if (accountId == null) {
            throw new RuntimeException("No account ID provided.");
        }
        requireAccount(accountId);
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new RuntimeException("No description provided.");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid amount provided.");
        }

        CurrencyType currencyType = CurrencyType.fromString(request.getCurrency());
        TransactionDirection direction = TransactionDirection.fromString(request.getDirection());

        Balance bal = balRepo.findByAccountIdAndCurrency(accountId, currencyType.name());
        if (bal == null) {
            throw new RuntimeException("Balance returned NULL.");
        }

        BigDecimal delta = request.getAmount();
        if (direction == TransactionDirection.OUT) {
            if (bal.getAvailableAmount().compareTo(delta) < 0) {
                throw new RuntimeException("Not enough money.");
            }
            delta = delta.negate();
        }

        balRepo.updateAvailableAmount(accountId, currencyType.name(), delta);
        Balance updatedBalance = balRepo.findByAccountIdAndCurrency(accountId, currencyType.name());
        if (updatedBalance == null) {
            throw new RuntimeException("Balance refresh failed.");
        }

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(currencyType.name());
        transaction.setDirection(direction.name());
        transaction.setDescription(request.getDescription());
        transaction.setBalanceAfter(updatedBalance.getAvailableAmount());
        txnRepo.insert(transaction);

        // emit events for downstream
        eventPublisher.send("UPDATE", "BALANCE",
                new BalanceEventPayload(accountId, currencyType.name(), updatedBalance.getAvailableAmount()));
        eventPublisher.send("INSERT", "TRANSACTION",
                new TransactionEventPayload(
                        accountId,
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDirection(),
                        transaction.getDescription(),
                        transaction.getBalanceAfter()
                ));

        log.info("transaction created id={}, accountId={}", transaction.getId(), accountId);
        return new TransactionResponse(
                transaction.getAccountId(),
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDirection(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }

    public List<TransactionResponse> getTransactions(Long accountId) {
        requireAccount(accountId);
        return txnRepo.findByAccountId(accountId).stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getAccountId(),
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDirection(),
                        transaction.getDescription(),
                        transaction.getBalanceAfter()
                ))
                .toList();
    }

    private void requireAccount(Long accountId) {
        Account acct = accountRepo.findById(accountId);
        if (acct == null) {
            throw new RuntimeException("Account ID does not exist.");
        }
    }

}
