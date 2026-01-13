package tuum.example.tuum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tuum.example.tuum.entity.Account;
import tuum.example.tuum.entity.Balance;
import tuum.example.tuum.model.AccountResponse;
import tuum.example.tuum.model.CreateAccountRequest;
import tuum.example.tuum.repository.AccountRepository;
import tuum.example.tuum.repository.BalanceRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository accountRepo;

    @Mock
    BalanceRepository balRepo;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    AccountService accountService;

    @Test
    void createAccountRejectsDuplicateCurrencies() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("EE");
        request.setCurrencies(List.of("EUR", "EUR"));

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Duplicate currencies");
    }

    @Test
    void createAccountCreatesBalancesAndPublishesEvents() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("EE");
        request.setCurrencies(List.of("EUR", "USD"));

        doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(10L);
            return 1;
        }).when(accountRepo).insert(any(Account.class));

        doAnswer(invocation -> {
            Balance balance = invocation.getArgument(0);
            balance.setId(100L);
            if (balance.getAvailableAmount() == null) {
                balance.setAvailableAmount(BigDecimal.ZERO);
            }
            return 1;
        }).when(balRepo).insert(any(Balance.class));

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.getAccountId()).isEqualTo(10L);
        assertThat(response.getBalances()).hasSize(2);
        verify(eventPublisher, times(3)).send(any(), any(), any());
    }

    @Test
    void getAccountReturnsBalances() {
        Account account = new Account();
        account.setId(1337L);
        account.setCustomerId(2L);
        account.setCountry("EE");
        when(accountRepo.findById(1337L)).thenReturn(account);

        Balance eur = new Balance();
        eur.setAccountId(1337L);
        eur.setCurrency("EUR");
        eur.setAvailableAmount(new BigDecimal("12.00"));
        when(balRepo.findByAccountId(1337L)).thenReturn(List.of(eur));

        AccountResponse response = accountService.getAccount(1337L);

        assertThat(response.getAccountId()).isEqualTo(1337L);
        assertThat(response.getBalances()).hasSize(1);
    }
}
