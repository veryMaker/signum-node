/*
 On April, 2024
 - This model incorporates already all migrations until V10 as done for MariaDB and H2
  As to JDBC Migrations (brs.db.sql.migration) with Flyway further model migrations have to start with V7_4 at least (better V8)
 */

CREATE TABLE IF NOT EXISTS account
(
  db_id           INTEGER PRIMARY KEY AUTOINCREMENT,
  id              INTEGER           NOT NULL,
  creation_height INTEGER           NOT NULL,
  public_key      BLOB,
  key_height      INTEGER,
  name            TEXT    DEFAULT NULL,
  description     TEXT,
  height          INTEGER           NOT NULL,
  latest          INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16393_account_id_latest_idx ON account (id, latest);
CREATE INDEX IF NOT EXISTS idx_16393_account_height_idx ON account (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16393_account_id_height_idx ON account (id, height DESC);

CREATE TABLE IF NOT EXISTS account_balance
(
  db_id               INTEGER PRIMARY KEY AUTOINCREMENT,
  id                  INTEGER           NOT NULL,
  balance             INTEGER           NOT NULL,
  unconfirmed_balance INTEGER           NOT NULL,
  forged_balance      INTEGER           NOT NULL,
  height              INTEGER           NOT NULL,
  latest              INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16411_account_balance_height_idx ON account_balance (height);
CREATE INDEX IF NOT EXISTS idx_16411_account_balance_id_latest_idx ON account_balance (id, latest);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16411_account_balance_id_height_idx ON account_balance (id, height DESC);


CREATE TABLE IF NOT EXISTS account_asset
(
  db_id                INTEGER PRIMARY KEY AUTOINCREMENT,
  account_id           INTEGER           NOT NULL,
  asset_id             INTEGER           NOT NULL,
  quantity             INTEGER           NOT NULL,
  unconfirmed_quantity INTEGER           NOT NULL,
  height               INTEGER           NOT NULL,
  latest               BOOLEAN DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16404_account_asset_assetid_idx ON account_asset (latest, asset_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16404_account_asset_id_height_idx ON account_asset (account_id, asset_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16404_account_asset_height_idx ON account_asset (height);
CREATE INDEX IF NOT EXISTS idx_16404_account_asset_quantity_idx ON account_asset (quantity);

CREATE TABLE IF NOT EXISTS alias
(
  db_id            INTEGER PRIMARY KEY AUTOINCREMENT,
  id               INTEGER           NOT NULL,
  account_id       INTEGER           NOT NULL,
  alias_name       VARCHAR(100)      NOT NULL,
  alias_name_lower VARCHAR(100)      NOT NULL,
  alias_uri        TEXT              NOT NULL,
  timestamp        INTEGER           NOT NULL,
  height           INTEGER           NOT NULL,
  latest           BOOLEAN DEFAULT 1 NOT NULL,
  tld              INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_16418_alias_account_id_idx ON alias (account_id, height DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16418_alias_id_height_idx ON alias (id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16418_alias_name_lower_tld_idx ON alias (alias_name_lower, tld, height DESC);
CREATE INDEX IF NOT EXISTS idx_16418_alias_latest_idx ON alias (latest, alias_name_lower);
CREATE INDEX IF NOT EXISTS idx_16418_alias_height_idx ON alias (height);
CREATE INDEX IF NOT EXISTS idx_16418_alias_name_lower_idx ON alias (alias_name_lower);

CREATE TABLE IF NOT EXISTS alias_offer
(
  db_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  id       INTEGER           NOT NULL,
  price    INTEGER           NOT NULL,
  buyer_id INTEGER,
  height   INTEGER           NOT NULL,
  latest   BOOLEAN DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16429_alias_offer_height_idx ON alias_offer (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16429_alias_offer_id_height_idx ON alias_offer (id, height DESC);

CREATE TABLE IF NOT EXISTS ask_order
(
  db_id           INTEGER PRIMARY KEY AUTOINCREMENT,
  id              INTEGER           NOT NULL,
  account_id      INTEGER           NOT NULL,
  asset_id        INTEGER           NOT NULL,
  price           INTEGER           NOT NULL,
  quantity        INTEGER           NOT NULL,
  creation_height INTEGER           NOT NULL,
  height          INTEGER           NOT NULL,
  latest          BOOLEAN DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16436_ask_order_account_id_idx ON ask_order (account_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16436_ask_order_creation_idx ON ask_order (creation_height);
CREATE INDEX IF NOT EXISTS idx_16436_ask_order_asset_id_price_idx ON ask_order (latest, asset_id, price);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16436_ask_order_id_height_idx ON ask_order (id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16436_ask_order_height_idx ON ask_order (height);

CREATE TABLE IF NOT EXISTS asset
(
  db_id       INTEGER PRIMARY KEY AUTOINCREMENT,
  id          INTEGER           NOT NULL,
  account_id  INTEGER           NOT NULL,
  name        VARCHAR(10)       NOT NULL,
  description TEXT,
  quantity    INTEGER           NOT NULL,
  decimals    INTEGER           NOT NULL,
  height      INTEGER           NOT NULL,
  mintable    BOOLEAN DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16443_asset_height_idx ON asset (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16443_asset_id_idx ON asset (id);
CREATE INDEX IF NOT EXISTS idx_16443_asset_account_id_idx ON asset (account_id);

CREATE TABLE IF NOT EXISTS asset_transfer
(
  db_id        INTEGER PRIMARY KEY AUTOINCREMENT,
  id           INTEGER NOT NULL,
  asset_id     INTEGER NOT NULL,
  sender_id    INTEGER NOT NULL,
  recipient_id INTEGER NOT NULL,
  quantity     INTEGER NOT NULL,
  timestamp    INTEGER NOT NULL,
  height       INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16453_asset_transfer_height_idx ON asset_transfer (height);
CREATE INDEX IF NOT EXISTS idx_16453_asset_transfer_recipient_id_idx ON asset_transfer (recipient_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16453_asset_transfer_id_idx ON asset_transfer (id);
CREATE INDEX IF NOT EXISTS idx_16453_asset_transfer_sender_id_idx ON asset_transfer (sender_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16453_asset_transfer_asset_id_idx ON asset_transfer (asset_id, height DESC);

CREATE TABLE IF NOT EXISTS at
(
  db_id              INTEGER PRIMARY KEY AUTOINCREMENT,
  id                 INTEGER           NOT NULL,
  creator_id         INTEGER           NOT NULL,
  name               TEXT    DEFAULT NULL,
  description        TEXT,
  version            INTEGER           NOT NULL,
  csize              INTEGER           NOT NULL,
  dsize              INTEGER           NOT NULL,
  c_user_stack_bytes INTEGER           NOT NULL,
  c_call_stack_bytes INTEGER           NOT NULL,
  creation_height    INTEGER           NOT NULL,
  ap_code            BLOB,
  height             INTEGER           NOT NULL,
  latest             INTEGER DEFAULT 1 NOT NULL,
  ap_code_hash_id    INTEGER
);

CREATE INDEX IF NOT EXISTS idx_16459_at_height_idx ON at (height);
CREATE INDEX IF NOT EXISTS idx_16459_at_ap_code_hash_id_index ON at (ap_code_hash_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16459_at_id_height_idx ON at (id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16459_at_latest_idx ON at (latest, id);
CREATE INDEX IF NOT EXISTS idx_16459_at_creator_id_height_idx ON at (creator_id, height DESC);

CREATE TABLE IF NOT EXISTS at_map
(
  db_id  INTEGER PRIMARY KEY AUTOINCREMENT,
  at_id  INTEGER           NOT NULL,
  key1   INTEGER           NOT NULL,
  key2   INTEGER,
  value  INTEGER,
  height INTEGER           NOT NULL,
  latest INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16470_at_map_at_keys_idx ON at_map (at_id, key1, key2);
CREATE INDEX IF NOT EXISTS idx_16470_at_map_at_latest_idx ON at_map (at_id, latest);
CREATE INDEX IF NOT EXISTS idx_16470_at_map_height_idx ON at_map (height);

CREATE TABLE IF NOT EXISTS at_state
(
  db_id                    INTEGER PRIMARY KEY AUTOINCREMENT,
  at_id                    INTEGER           NOT NULL,
  state                    BLOB              NOT NULL,
  prev_height              INTEGER           NOT NULL,
  next_height              INTEGER           NOT NULL,
  sleep_between            INTEGER           NOT NULL,
  prev_balance             INTEGER           NOT NULL,
  freeze_when_same_balance INTEGER           NOT NULL,
  min_activate_amount      INTEGER           NOT NULL,
  height                   INTEGER           NOT NULL,
  latest                   INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16477_at_state_height_idx ON at_state (height);
CREATE INDEX IF NOT EXISTS idx_16477_at_state_id_next_height_height_idx ON at_state (at_id, next_height, height DESC);
CREATE INDEX IF NOT EXISTS idx_16477_at_state_id_latest_idx ON at_state (at_id, latest);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16477_at_state_at_id_height_idx ON at_state (at_id, height DESC);

CREATE TABLE IF NOT EXISTS bid_order
(
  db_id           INTEGER PRIMARY KEY AUTOINCREMENT,
  id              INTEGER           NOT NULL,
  account_id      INTEGER           NOT NULL,
  asset_id        INTEGER           NOT NULL,
  price           INTEGER           NOT NULL,
  quantity        INTEGER           NOT NULL,
  creation_height INTEGER           NOT NULL,
  height          INTEGER           NOT NULL,
  latest          INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16487_bid_order_account_id_idx ON bid_order (account_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16487_bid_order_asset_id_price_idx ON bid_order (latest, asset_id, price);
CREATE INDEX IF NOT EXISTS idx_16487_bid_order_creation_idx ON bid_order (creation_height);
CREATE INDEX IF NOT EXISTS idx_16487_bid_order_height_idx ON bid_order (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16487_bid_order_id_height_idx ON bid_order (id, height DESC);

CREATE TABLE IF NOT EXISTS block
(
  db_id                 INTEGER PRIMARY KEY AUTOINCREMENT,
  id                    INTEGER           NOT NULL UNIQUE,
  version               INTEGER           NOT NULL,
  timestamp             INTEGER           NOT NULL,
  previous_block_id     INTEGER,
  total_amount          INTEGER           NOT NULL,
  total_fee             INTEGER           NOT NULL,
  payload_length        INTEGER           NOT NULL,
  generator_public_key  BLOB              NOT NULL,
  previous_block_hash   BLOB,
  cumulative_difficulty BLOB              NOT NULL,
  base_target           INTEGER           NOT NULL,
  next_block_id         INTEGER,
  height                INTEGER           NOT NULL,
  generation_signature  BLOB              NOT NULL,
  block_signature       BLOB              NOT NULL,
  payload_hash          BLOB              NOT NULL,
  generator_id          INTEGER           NOT NULL,
  nonce                 INTEGER           NOT NULL,
  ats                   BLOB,
  total_fee_cash_back   INTEGER DEFAULT 0 NOT NULL,
  total_fee_burnt       INTEGER DEFAULT 0 NOT NULL
  -- THIS TABLE USES TRIGGERS FOR CASCADED ACTIONS
);

CREATE INDEX IF NOT EXISTS idx_16494_block_generator_id_height_idx ON block (generator_id, height DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16494_block_timestamp_idx ON block (timestamp DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16494_block_height_idx ON block (height);
CREATE INDEX IF NOT EXISTS idx_16494_block_next_block_id_idx ON block (next_block_id);
CREATE INDEX IF NOT EXISTS idx_16494_block_previous_block_id_idx ON block (previous_block_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16494_block_id_idx ON block (id);
CREATE INDEX IF NOT EXISTS idx_16494_block_generator_id_idx ON block (generator_id);

CREATE TABLE IF NOT EXISTS escrow
(
  db_id            INTEGER PRIMARY KEY AUTOINCREMENT,
  id               INTEGER           NOT NULL,
  sender_id        INTEGER           NOT NULL,
  recipient_id     INTEGER           NOT NULL,
  amount           INTEGER           NOT NULL,
  required_signers INTEGER,
  deadline         INTEGER           NOT NULL,
  deadline_action  INTEGER           NOT NULL,
  height           INTEGER           NOT NULL,
  latest           INTEGER DEFAULT 1 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_16505_escrow_id_height_idx ON escrow (id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16505_escrow_sender_id_height_idx ON escrow (sender_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16505_escrow_deadline_height_idx ON escrow (deadline, height DESC);
CREATE INDEX IF NOT EXISTS idx_16505_escrow_recipient_id_height_idx ON escrow (recipient_id, height DESC);

CREATE TABLE IF NOT EXISTS escrow_decision
(
  db_id      INTEGER PRIMARY KEY AUTOINCREMENT,
  escrow_id  INTEGER           NOT NULL,
  account_id INTEGER           NOT NULL,
  decision   INTEGER           NOT NULL,
  height     INTEGER           NOT NULL,
  latest     INTEGER DEFAULT 1 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_16512_escrow_decision_escrow_id_account_id_height_idx ON escrow_decision (escrow_id, account_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16512_escrow_decision_escrow_id_height_idx ON escrow_decision (escrow_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16512_escrow_decision_account_id_height_idx ON escrow_decision (account_id, height DESC);

CREATE TABLE IF NOT EXISTS goods
(
  db_id       INTEGER PRIMARY KEY AUTOINCREMENT,
  id          INTEGER           NOT NULL,
  seller_id   INTEGER           NOT NULL,
  name        TEXT              NOT NULL,
  description TEXT,
  tags        TEXT    DEFAULT NULL,
  timestamp   INTEGER           NOT NULL,
  quantity    INTEGER           NOT NULL,
  price       INTEGER           NOT NULL,
  delisted    INTEGER           NOT NULL,
  height      INTEGER           NOT NULL,
  latest      INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16527_goods_timestamp_idx ON goods (timestamp DESC, height DESC);
CREATE INDEX IF NOT EXISTS idx_16527_goods_seller_id_name_idx ON goods (seller_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16527_goods_id_height_idx ON goods (id, height DESC);

CREATE TABLE IF NOT EXISTS peer
(
  address TEXT PRIMARY KEY NOT NULL
);

CREATE TABLE IF NOT EXISTS purchase
(
  db_id                INTEGER PRIMARY KEY AUTOINCREMENT,
  id                   INTEGER           NOT NULL,
  buyer_id             INTEGER           NOT NULL,
  goods_id             INTEGER           NOT NULL,
  seller_id            INTEGER           NOT NULL,
  quantity             INTEGER           NOT NULL,
  price                INTEGER           NOT NULL,
  deadline             INTEGER           NOT NULL,
  note                 BLOB,
  nonce                BLOB,
  timestamp            INTEGER           NOT NULL,
  pending              INTEGER           NOT NULL,
  goods                BLOB,
  goods_nonce          BLOB,
  refund_note          BLOB,
  refund_nonce         BLOB,
  has_feedback_notes   INTEGER DEFAULT 0 NOT NULL,
  has_public_feedbacks INTEGER DEFAULT 0 NOT NULL,
  discount             INTEGER           NOT NULL,
  refund               INTEGER           NOT NULL,
  height               INTEGER           NOT NULL,
  latest               INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16549_purchase_deadline_idx ON purchase (deadline, height DESC);
CREATE INDEX IF NOT EXISTS idx_16549_purchase_seller_id_height_idx ON purchase (seller_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16549_purchase_buyer_id_height_idx ON purchase (buyer_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16549_purchase_timestamp_idx ON purchase (timestamp DESC, id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16549_purchase_id_height_idx ON purchase (id, height DESC);

CREATE TABLE IF NOT EXISTS purchase_feedback
(
  db_id          INTEGER PRIMARY KEY AUTOINCREMENT,
  id             INTEGER           NOT NULL,
  feedback_data  BLOB              NOT NULL,
  feedback_nonce BLOB              NOT NULL,
  height         INTEGER           NOT NULL,
  latest         INTEGER DEFAULT 1 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_16561_purchase_feedback_id_height_idx ON purchase_feedback (id, height DESC);

CREATE TABLE IF NOT EXISTS purchase_public_feedback
(
  db_id           INTEGER PRIMARY KEY AUTOINCREMENT,
  id              INTEGER           NOT NULL,
  public_feedback TEXT              NOT NULL,
  height          INTEGER           NOT NULL,
  latest          INTEGER DEFAULT 1 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_16571_purchase_public_feedback_id_height_idx ON purchase_public_feedback (id, height DESC);

CREATE TABLE IF NOT EXISTS reward_recip_assign
(
  db_id         INTEGER PRIMARY KEY AUTOINCREMENT,
  account_id    INTEGER           NOT NULL,
  prev_recip_id INTEGER           NOT NULL,
  recip_id      INTEGER           NOT NULL,
  from_height   INTEGER           NOT NULL,
  height        INTEGER           NOT NULL,
  latest        INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16581_reward_recip_assign_height_idx ON reward_recip_assign (height);
CREATE INDEX IF NOT EXISTS idx_16581_reward_recip_assign_recip_id_height_idx ON reward_recip_assign (recip_id, height DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16581_reward_recip_assign_account_id_height_idx ON reward_recip_assign (account_id, height DESC);

CREATE TABLE IF NOT EXISTS subscription
(
  db_id        INTEGER PRIMARY KEY AUTOINCREMENT,
  id           INTEGER           NOT NULL,
  sender_id    INTEGER           NOT NULL,
  recipient_id INTEGER           NOT NULL,
  amount       INTEGER           NOT NULL,
  frequency    INTEGER           NOT NULL,
  time_next    INTEGER           NOT NULL,
  height       INTEGER           NOT NULL,
  latest       INTEGER DEFAULT 1 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16588_subscription_time_next_index ON subscription (time_next);
CREATE INDEX IF NOT EXISTS idx_16588_subscription_sender_id_height_idx ON subscription (sender_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16588_subscription_recipient_id_height_idx ON subscription (recipient_id, height DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16588_subscription_id_height_idx ON subscription (id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16588_subscription_latest_index ON subscription (latest);
CREATE INDEX IF NOT EXISTS idx_16588_subscription_height_idx ON subscription (height);
CREATE INDEX IF NOT EXISTS idx_16588_subscription_id_latest_idx ON subscription (id, latest);

CREATE TABLE IF NOT EXISTS trade
(
  db_id            INTEGER PRIMARY KEY AUTOINCREMENT,
  asset_id         INTEGER NOT NULL,
  block_id         INTEGER NOT NULL,
  ask_order_id     INTEGER NOT NULL,
  bid_order_id     INTEGER NOT NULL,
  ask_order_height INTEGER NOT NULL,
  bid_order_height INTEGER NOT NULL,
  seller_id        INTEGER NOT NULL,
  buyer_id         INTEGER NOT NULL,
  quantity         INTEGER NOT NULL,
  price            INTEGER NOT NULL,
  timestamp        INTEGER NOT NULL,
  height           INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_16595_trade_asset_id_idx ON trade (asset_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16595_trade_seller_id_idx ON trade (seller_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16595_trade_buyer_id_idx ON trade (buyer_id, height DESC);
CREATE INDEX IF NOT EXISTS idx_16595_trade_height_idx ON trade (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16595_trade_ask_bid_idx ON trade (ask_order_id, bid_order_id);

CREATE TABLE IF NOT EXISTS "transaction"
(
  db_id                           INTEGER PRIMARY KEY AUTOINCREMENT,
  id                              INTEGER           NOT NULL UNIQUE,
  deadline                        INTEGER           NOT NULL,
  sender_public_key               BLOB              NOT NULL,
  recipient_id                    INTEGER,
  amount                          INTEGER           NOT NULL,
  fee                             INTEGER           NOT NULL,
  height                          INTEGER           NOT NULL,
  block_id                        INTEGER           NOT NULL REFERENCES block (id) ON DELETE CASCADE,
  signature                       BLOB,
  timestamp                       INTEGER           NOT NULL,
  type                            INTEGER           NOT NULL,
  subtype                         INTEGER           NOT NULL,
  sender_id                       INTEGER           NOT NULL,
  block_timestamp                 INTEGER           NOT NULL,
  full_hash                       BLOB              NOT NULL,
  referenced_transaction_fullhash BLOB,
  attachment_bytes                BLOB,
  version                         INTEGER           NOT NULL,
  has_message                     INTEGER DEFAULT 0 NOT NULL,
  has_encrypted_message           INTEGER DEFAULT 0 NOT NULL,
  has_public_key_announcement     INTEGER DEFAULT 0 NOT NULL,
  ec_block_height                 INTEGER,
  ec_block_id                     INTEGER,
  has_encrypttoself_message       INTEGER DEFAULT 0 NOT NULL,
  cash_back_id                    INTEGER DEFAULT 0 NOT NULL
  -- THIS TABLE USES TRIGGERS FOR CASCADED ACTIONS
);

CREATE INDEX IF NOT EXISTS idx_16601_transaction_recipient_id_idx ON "transaction" (recipient_id);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_block_id_idx ON "transaction" (block_id);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_ref_tx_fullhash_idx ON "transaction" (referenced_transaction_fullhash);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16601_transaction_full_hash_idx ON "transaction" (full_hash);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_block_timestamp_idx ON "transaction" (block_timestamp);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_type_subtype_idx ON "transaction" (type, subtype);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_height_idx ON "transaction" (height);
CREATE INDEX IF NOT EXISTS idx_16601_tx_cash_back_index ON "transaction" (cash_back_id);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_sender_type ON "transaction" (sender_id, type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16601_transaction_id_idx ON "transaction" (id);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_recipient_id_amount_height_idx ON "transaction" (recipient_id, amount, height DESC);
CREATE INDEX IF NOT EXISTS idx_16601_transaction_sender_id_idx ON "transaction" (sender_id);

CREATE TABLE IF NOT EXISTS indirect_incoming
(
  db_id          INTEGER PRIMARY KEY AUTOINCREMENT,
  account_id     INTEGER NOT NULL,
  transaction_id INTEGER NOT NULL,
  height         INTEGER NOT NULL,
  amount         INTEGER DEFAULT 0,
  quantity       INTEGER DEFAULT 0
  -- THIS TABLE USES TRIGGERS FOR CASCADED ACTIONS
);

CREATE INDEX IF NOT EXISTS idx_16538_indirect_incoming_id_index ON indirect_incoming (account_id);
CREATE INDEX IF NOT EXISTS idx_16538_indirect_incoming_height_idx ON indirect_incoming (height);
CREATE UNIQUE INDEX IF NOT EXISTS idx_16538_indirect_incoming_db_id_uindex ON indirect_incoming (account_id, transaction_id);
CREATE INDEX IF NOT EXISTS idx_16538_indirect_incoming_tx_idx ON indirect_incoming (transaction_id);

CREATE TABLE IF NOT EXISTS unconfirmed_transaction
(
  db_id              INTEGER PRIMARY KEY AUTOINCREMENT,
  id                 INTEGER NOT NULL,
  expiration         INTEGER NOT NULL,
  transaction_height INTEGER NOT NULL,
  fee_per_byte       INTEGER NOT NULL,
  timestamp          INTEGER NOT NULL,
  transaction_bytes  BLOB    NOT NULL,
  height             INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_16615_unconfirmed_transaction_id_idx ON unconfirmed_transaction (id);
CREATE INDEX IF NOT EXISTS idx_16615_unconfirmed_transaction_height_fee_timestamp_idx ON unconfirmed_transaction (transaction_height, fee_per_byte, timestamp DESC);

-- CASCADE DELETIONS/UPDATES - AS OF MULTITHREADED INSERTS WE CANNOT WORK WITH FOREIGN KEY CONSTRAINTS...BUT ROLLBACKS RELY ON CASCADED SETTINGS

CREATE TRIGGER cascade_delete_previous_block
  AFTER DELETE
  ON block
  FOR EACH ROW
BEGIN
  DELETE FROM block WHERE previous_block_id = OLD.id;
END;

CREATE TRIGGER cascade_set_next_block_null
  AFTER DELETE
  ON block
  FOR EACH ROW
BEGIN
  UPDATE block SET next_block_id = NULL WHERE next_block_id = OLD.id;
END;

CREATE TRIGGER cascade_delete_tx
  AFTER DELETE
  ON block
  FOR EACH ROW
BEGIN
  DELETE FROM "transaction" where block_id = old.id;
END;

CREATE TRIGGER cascade_delete_indirect
  AFTER DELETE
  ON "transaction"
  FOR EACH ROW
BEGIN
  DELETE FROM "indirect_incoming" where indirect_incoming.transaction_id = old.id;
END;
