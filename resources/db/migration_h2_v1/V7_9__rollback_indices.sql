CREATE INDEX IF NOT EXISTS account_height_idx ON account(height);
CREATE INDEX IF NOT EXISTS account_balance_height_idx ON account_balance(height);
CREATE INDEX IF NOT EXISTS account_asset_height_idx ON account_asset(height);

CREATE INDEX IF NOT EXISTS indirect_incoming_height_idx ON indirect_incoming(height);

CREATE INDEX IF NOT EXISTS alias_height_idx ON alias(height);
CREATE INDEX IF NOT EXISTS alias_offer_height_idx ON alias_offer(height);

CREATE INDEX IF NOT EXISTS asset_height_idx ON asset(height);
CREATE INDEX IF NOT EXISTS asset_transfer_height_idx ON asset_transfer(height);
CREATE INDEX IF NOT EXISTS ask_order_height_idx ON ask_order(height);
CREATE INDEX IF NOT EXISTS bid_order_height_idx ON bid_order(height);
CREATE INDEX IF NOT EXISTS trade_height_idx ON trade(height);

CREATE INDEX IF NOT EXISTS at_height_idx ON at(height);
CREATE INDEX IF NOT EXISTS at_state_height_idx ON at_state(height);
CREATE INDEX IF NOT EXISTS at_map_height_idx ON at_map(height);

CREATE INDEX IF NOT EXISTS subscription_height_idx ON subscription(height);

CREATE INDEX IF NOT EXISTS reward_recip_assign_height_idx ON reward_recip_assign(height);
