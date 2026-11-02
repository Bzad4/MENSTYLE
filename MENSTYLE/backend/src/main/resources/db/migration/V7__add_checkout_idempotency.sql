ALTER TABLE dbo.orders ADD idempotency_key VARCHAR(100) NULL;

/*
 * Keep index compilation until after ALTER TABLE has completed. SQL Server
 * may compile all static statements in a Flyway migration batch together and
 * otherwise resolve idempotency_key before the new column exists.
 */
EXEC(N'CREATE UNIQUE INDEX ux_orders_user_idempotency_key
       ON dbo.orders(user_id, idempotency_key)
       WHERE idempotency_key IS NOT NULL');
