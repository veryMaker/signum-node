drop index if exists "at_id_height_idx";
create unique hash index "at_id_height_idx"
  on "at" ("id" asc, "height" desc);
