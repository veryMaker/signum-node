drop index if exists at_id_height_idx on at;
create unique index at_id_height_idx
  on at (id asc, height desc) using hash;
