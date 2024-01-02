create index if not exists alias_name_lower_tld_idx
  on alias (alias_name_lower , tld, height desc);

create unique index if not exists alias_id_height_idx
  on alias (id, height);

