CREATE INDEX IF NOT EXISTS transaction_type_subtype_idx ON transaction(type, subtype) using btree;

CREATE UNIQUE INDEX IF NOT EXISTS account_balance_id_height_idx ON account_balance(id, height);
