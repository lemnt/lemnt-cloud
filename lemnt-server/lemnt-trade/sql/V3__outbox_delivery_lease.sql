-- Stop all old workers before applying; deploy new workers only after migration.
ALTER TABLE trade_outbox
 ADD COLUMN lease_token VARCHAR(36) NULL,
 ADD COLUMN lease_until DATETIME NULL,
 ADD COLUMN next_attempt_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 ADD INDEX idx_trade_outbox_due(status,next_attempt_at,retry_count),
 ADD INDEX idx_trade_outbox_lease(status,lease_until);
-- Interrupted legacy claims have no lease. Retry through the new bounded processor.
UPDATE trade_outbox SET status='FAILED', next_attempt_at=CURRENT_TIMESTAMP
 WHERE status='PROCESSING';
