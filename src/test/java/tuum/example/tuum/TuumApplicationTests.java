package tuum.example.tuum;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tuum.example.tuum.config.RabbitConfig;
import tuum.example.tuum.exception.ErrorMessage;
import tuum.example.tuum.model.AccountResponse;
import tuum.example.tuum.model.CreateAccountRequest;
import tuum.example.tuum.model.CreateTransactionRequest;
import tuum.example.tuum.model.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TuumApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("tuum")
			.withUsername("tuum")
			.withPassword("tuum");

	@Container
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management");

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", () -> "guest");
		registry.add("spring.rabbitmq.password", () -> "guest");
	}

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Test
	void createAccountPublishesEventAndReturnsBalances() {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setCustomerId(420L);
		request.setCountry("EE");
		request.setCurrencies(List.of("EUR", "USD"));

		ResponseEntity<AccountResponse> response = restTemplate.postForEntity("/accounts", request, AccountResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getAccountId()).isNotNull();
		assertThat(response.getBody().getBalances()).hasSize(2);

		Object event = rabbitTemplate.receiveAndConvert(RabbitConfig.QUEUE_NAME, 2000);
		assertThat(event).isNotNull();
	}

	@Test
	void createTransactionUpdatesBalanceAndReturnsTransaction() {
		Long accountId = createAccount();

		CreateTransactionRequest request = new CreateTransactionRequest();
		request.setAmount(new BigDecimal("1.30"));
		request.setCurrency("EUR");
		request.setDirection("IN");
		request.setDescription("You won the lottery!!!");

		ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
				"/accounts/" + accountId + "/transactions",
				request,
				TransactionResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getBalanceAfterTransaction()).isEqualByComparingTo("1.30");
	}

	@Test
	void createTransactionRejectsInsufficientFunds() {
		Long accountId = createAccount();

		CreateTransactionRequest request = new CreateTransactionRequest();
		request.setAmount(new BigDecimal("999999.99"));
		request.setCurrency("EUR");
		request.setDirection("OUT");
		request.setDescription("Withdrawal");

		ResponseEntity<ErrorMessage> response = restTemplate.postForEntity(
				"/accounts/" + accountId + "/transactions",
				request,
				ErrorMessage.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("Insufficient funds");
	}

	@Test
	void getAccountMissingReturnsError() {
		ResponseEntity<ErrorMessage> response = restTemplate.getForEntity("/accounts/9999", ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("Account not found");
	}

	@Test
	void createTransactionRejectsInvalidCurrency() {
		Long accountId = createAccount();

		CreateTransactionRequest request = new CreateTransactionRequest();
		request.setAmount(new BigDecimal("10.00"));
		request.setCurrency("JPY");
		request.setDirection("IN");
		request.setDescription("Invalid currency");

		ResponseEntity<ErrorMessage> response = restTemplate.postForEntity(
				"/accounts/" + accountId + "/transactions",
				request,
				ErrorMessage.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("Invalid currency");
	}

	@Test
	void getTransactionsReturnsHistory() {
		Long accountId = createAccount();

		CreateTransactionRequest request = new CreateTransactionRequest();
		request.setAmount(new BigDecimal("1.00"));
		request.setCurrency("EUR");
		request.setDirection("IN");
		request.setDescription("test");

		ResponseEntity<TransactionResponse> createResponse = restTemplate.postForEntity(
				"/accounts/" + accountId + "/transactions",
				request,
				TransactionResponse.class
		);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<TransactionResponse[]> listResponse = restTemplate.getForEntity(
				"/accounts/" + accountId + "/transactions",
				TransactionResponse[].class
		);

		assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(listResponse.getBody()).isNotNull();
		assertThat(listResponse.getBody().length).isEqualTo(1);
	}

	private Long createAccount() {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setCustomerId(1337L);
		request.setCountry("EE");
		request.setCurrencies(List.of("EUR"));

		ResponseEntity<AccountResponse> response = restTemplate.postForEntity("/accounts", request, AccountResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody().getAccountId();
	}
}
