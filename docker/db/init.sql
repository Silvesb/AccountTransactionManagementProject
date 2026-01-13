CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    country VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS balance (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    currency VARCHAR(3) NOT NULL,
    available_amount NUMERIC(19, 2) NOT NULL,
    UNIQUE (account_id, currency)
);

CREATE TABLE IF NOT EXISTS account_transaction (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    direction VARCHAR(3) NOT NULL,
    description VARCHAR(255) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
