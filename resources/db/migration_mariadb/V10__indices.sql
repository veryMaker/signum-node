CREATE INDEX IF NOT EXISTS at_latest_idx ON at(latest, id) using btree;

CREATE INDEX IF NOT EXISTS at_map_at_keys_idx ON at_map(at_id, key1, key2) using btree;
CREATE INDEX IF NOT EXISTS at_map_at_latest_idx ON at_map(at_id, latest) using btree;

CREATE INDEX IF NOT EXISTS transaction_height_idx ON transaction(height) using btree;
CREATE INDEX IF NOT EXISTS transaction_ref_tx_fullhash_idx ON transaction(referenced_transaction_fullhash) using btree;

CREATE INDEX IF NOT EXISTS account_asset_assetid_idx ON account_asset(latest, asset_id) using btree;

DROP INDEX IF EXISTS indirect_incoming_index ON indirect_incoming;
CREATE INDEX IF NOT EXISTS indirect_incoming_tx_idx ON indirect_incoming(transaction_id) using btree;

CREATE INDEX IF NOT EXISTS alias_latest_idx ON alias(latest, alias_name_lower) using btree;

DROP INDEX IF EXISTS ask_order_asset_id_price_idx ON ask_order;
CREATE INDEX IF NOT EXISTS ask_order_asset_id_price_idx ON ask_order(latest, asset_id, price) using btree;

DROP INDEX IF EXISTS bid_order_asset_id_price_idx ON bid_order;
CREATE INDEX IF NOT EXISTS bid_order_asset_id_price_idx ON bid_order(latest, asset_id, price) using btree;
