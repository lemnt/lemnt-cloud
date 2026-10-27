-- Additive trade migration. Deploy the mall cancellation/batch contract before new trade workers.
CREATE TABLE mall_checkout_account (member_id BIGINT NOT NULL PRIMARY KEY) ENGINE=InnoDB;
CREATE TABLE mall_cart (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 member_id BIGINT NOT NULL,
 product_id BIGINT NOT NULL,
 quantity INT NOT NULL,
 selected BOOLEAN NOT NULL DEFAULT TRUE,
 version BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_trade_cart_member_product(member_id,product_id),
 CONSTRAINT chk_trade_cart_quantity CHECK(quantity BETWEEN 1 AND 999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_coupon_template (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 code VARCHAR(64) NOT NULL,
 name VARCHAR(100) NOT NULL,
 kind VARCHAR(16) NOT NULL,
 threshold DECIMAL(19,2) NOT NULL,
 fixed_amount DECIMAL(19,2) NULL,
 discount_percent INT NULL,
 discount_limit DECIMAL(19,2) NULL,
 total_count INT NOT NULL,
 issued_count INT NOT NULL DEFAULT 0,
 valid_from DATETIME NOT NULL,
 valid_until DATETIME NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 UNIQUE KEY uk_trade_coupon_template_code(code),
 CONSTRAINT chk_trade_coupon_template_kind CHECK(kind IN ('FIXED','PERCENT')),
 CONSTRAINT chk_trade_coupon_template_counts CHECK(total_count>0 AND issued_count>=0 AND issued_count<=total_count),
 CONSTRAINT chk_trade_coupon_template_dates CHECK(valid_until>valid_from),
 CONSTRAINT chk_trade_coupon_template_amount CHECK(threshold>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_coupon (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 template_id BIGINT NOT NULL,
 member_id BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
 order_no VARCHAR(40) NULL,
 UNIQUE KEY uk_trade_coupon_member_template(member_id,template_id),
 UNIQUE KEY uk_trade_coupon_order(order_no),
 CONSTRAINT fk_trade_coupon_template FOREIGN KEY(template_id) REFERENCES mall_coupon_template(id),
 CONSTRAINT chk_trade_coupon_status CHECK(status IN ('AVAILABLE','LOCKED','USED')),
 KEY idx_trade_coupon_member_status(member_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_checkout (
 order_no VARCHAR(40) NOT NULL PRIMARY KEY,
 member_id BIGINT NOT NULL,
 request_no VARCHAR(64) NOT NULL,
 fingerprint VARCHAR(64) NOT NULL,
 original_amount DECIMAL(19,2) NOT NULL,
 discount_amount DECIMAL(19,2) NOT NULL,
 payable_amount DECIMAL(19,2) NOT NULL,
 coupon_id BIGINT NULL,
 UNIQUE KEY uk_trade_checkout_request(member_id,request_no),

 CONSTRAINT fk_trade_checkout_coupon FOREIGN KEY(coupon_id) REFERENCES mall_coupon(id),
 CONSTRAINT chk_trade_checkout_amount CHECK(original_amount>0 AND discount_amount>=0 AND payable_amount>0 AND original_amount=discount_amount+payable_amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_checkout_cart (
 order_no VARCHAR(40) NOT NULL,
 cart_id BIGINT NOT NULL,
 cart_version BIGINT NOT NULL,
 PRIMARY KEY(order_no,cart_id),
 KEY idx_trade_checkout_cart_id(cart_id,order_no),
 CONSTRAINT fk_trade_checkout_cart_order FOREIGN KEY(order_no) REFERENCES mall_checkout(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
