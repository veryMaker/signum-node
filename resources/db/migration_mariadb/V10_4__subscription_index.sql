
drop index if exists subscription_id_height_idx on subscription;
create unique index subscription_id_height_idx
  on subscription (id asc, height desc) using hash;

create index if not exists subscription_id_latest_idx
  on subscription (id, latest) using hash;
