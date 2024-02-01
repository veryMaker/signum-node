-- Kept because of JDBC flyway migration V7_3 - see src.brs.db.sql.migration
ALTER TABLE account DROP COLUMN IF EXISTS balance;
ALTER TABLE account DROP COLUMN IF EXISTS unconfirmed_balance;
ALTER TABLE account DROP COLUMN IF EXISTS forged_balance;
