CREATE INDEX IF NOT EXISTS block_generator_id_height_idx ON block(generator_id, height) using btree;
