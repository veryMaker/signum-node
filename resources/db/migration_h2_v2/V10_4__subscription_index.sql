drop index if exists "subscription_id_height_idx";
create unique hash index "subscription_id_height_idx"
  on "subscription" ("id" asc, "height" desc);

create hash index "subscription_id_latest_idx"
  on "subscription" ("id", "latest");
