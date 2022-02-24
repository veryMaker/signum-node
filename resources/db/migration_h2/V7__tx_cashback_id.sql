ALTER TABLE transaction ADD cash_back_id BIGINT DEFAULT '0';

CREATE INDEX tx_cash_back_index ON transaction (cash_back_id);
