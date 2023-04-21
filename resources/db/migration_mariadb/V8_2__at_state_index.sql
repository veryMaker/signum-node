CREATE INDEX IF NOT EXISTS at_state_id_latest_idx ON at_state(at_id, latest) using btree;
