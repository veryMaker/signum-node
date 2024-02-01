-- Kept because of JDBC flyway migration V7_3
create table if not exists account_balance
(
  db_id               bigserial
    constraint idx_16411_primary
      primary key,
  id                  bigint               not null,
  balance             bigint               not null,
  unconfirmed_balance bigint               not null,
  forged_balance      bigint               not null,
  height              bigint               not null,
  latest              boolean default true not null
);

create index if not exists idx_16411_account_balance_height_idx
  on account_balance (height);

create index if not exists idx_16411_account_balance_id_latest_idx
  on account_balance (id, latest);

create unique index if not exists idx_16411_account_balance_id_height_idx
  on account_balance (id, height desc);
