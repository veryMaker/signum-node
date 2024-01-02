alter table indirect_incoming
  add foreign key (transaction_id) references transaction (id) on delete cascade;
