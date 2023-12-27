// Kept because of JDBC flyway migration V7_3
alter table "account" drop column if exists balance;
alter table "account" drop column if exists unconfirmed_balance;
alter table "account" drop column if exists forged_balance;
