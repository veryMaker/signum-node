DROP INDEX IF EXISTS asset_transfer_id_idx ON asset_transfer;

CREATE INDEX IF NOT EXISTS asset_transfer_id_idx ON asset_transfer (id);
