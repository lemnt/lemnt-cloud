-- Stop old trade workers first; review historic paid/stock integrity before applying backfill.
CREATE TABLE trade_inventory_confirmation (
 order_no VARCHAR(40) NOT NULL PRIMARY KEY,
 confirmed_at DATETIME NOT NULL,
 CONSTRAINT fk_trade_inventory_confirmation_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO trade_inventory_confirmation(order_no,confirmed_at)
 SELECT o.order_no,CURRENT_TIMESTAMP FROM trade_order o WHERE o.status='PAID'
 AND EXISTS(SELECT 1 FROM trade_outbox e WHERE e.aggregate_no=o.order_no AND e.event_type='TRADE_ORDER_PAID' AND e.status='DELIVERED');
CREATE TABLE trade_shipment (
 order_no VARCHAR(40) NOT NULL PRIMARY KEY,
 request_no VARCHAR(64) NOT NULL,
 fingerprint VARCHAR(64) NOT NULL,
 carrier_code VARCHAR(40) NOT NULL,
 tracking_no VARCHAR(100) NOT NULL,
 recipient_name VARCHAR(100) NOT NULL,
 phone VARCHAR(32) NOT NULL,
 address VARCHAR(500) NOT NULL,
 shipped_at DATETIME NOT NULL,
 received_at DATETIME NULL,
 CONSTRAINT fk_trade_shipment_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE trade_refund_request (
 refund_no VARCHAR(40) NOT NULL PRIMARY KEY,
 order_no VARCHAR(40) NOT NULL,
 request_no VARCHAR(64) NOT NULL,
 member_id BIGINT NOT NULL,
 payment_no VARCHAR(40) NOT NULL,
 amount DECIMAL(19,2) NOT NULL,
 reason VARCHAR(500) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
 active_order_no VARCHAR(40) NULL,
 decision_reason VARCHAR(500) NULL,
 requested_at DATETIME NOT NULL,
 decided_at DATETIME NULL,
 UNIQUE KEY uk_trade_refund_order_request(order_no,request_no),
 UNIQUE KEY uk_trade_refund_active(active_order_no),
 KEY idx_trade_refund_status_time(status,requested_at),
 CONSTRAINT fk_trade_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
 CONSTRAINT fk_trade_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
 CONSTRAINT chk_trade_refund_amount CHECK(amount>0),
 CONSTRAINT chk_trade_refund_status CHECK(status IN ('REQUESTED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
