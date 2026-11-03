-- 第164批结构设计草案，仅供审查；未在数据库执行，不属于正式迁移目录。
-- 基于当前 trade V1..V17；不是幂等脚本，不可重复执行。
-- 正式实施需分别编制新库/已有库迁移并验证目标MySQL方言、锁表和回填顺序。
-- 旧行新字段保持NULL表示未知，新版订单由服务显式写完整金额和0计数。

ALTER TABLE trade_order
 ADD COLUMN original_amount DECIMAL(19,2) NULL COMMENT '下单时商品原价合计，单位元，历史未知为NULL',
 ADD COLUMN discount_amount DECIMAL(19,2) NULL COMMENT '商品优惠总额，含实际积分抵现金额，单位元，历史未知为NULL',
 ADD COLUMN delivery_original_price DECIMAL(19,2) NULL COMMENT '运费减免前应收运费，单位元，历史未知为NULL',
 ADD COLUMN delivery_discount_price DECIMAL(19,2) NULL COMMENT '运费优惠金额，单位元，历史未知为NULL',
 ADD COLUMN adjustment_amount DECIMAL(19,2) NULL COMMENT '人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
 ADD COLUMN pricing_snapshot_status VARCHAR(20) NOT NULL DEFAULT 'LEGACY_PARTIAL' COMMENT '金额快照状态：COMPLETE完整、LEGACY_PARTIAL历史不完整',
 ADD COLUMN pricing_snapshot_version INT NOT NULL DEFAULT 0 COMMENT '金额快照版本，历史未记录为0，支付单生成前调价递增',
 ADD CONSTRAINT chk_trade_order_pricing_status CHECK(pricing_snapshot_status IN ('COMPLETE','LEGACY_PARTIAL')),
 ADD CONSTRAINT chk_trade_order_discount CHECK(discount_amount IS NULL OR discount_amount>=0),
 ADD CONSTRAINT chk_trade_order_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price));

ALTER TABLE trade_order_item
 ADD COLUMN discount_amount DECIMAL(19,2) NULL COMMENT '本行商品优惠合计，单位元，历史未知为NULL',
 ADD COLUMN delivery_original_price DECIMAL(19,2) NULL COMMENT '分摊至本行的原始应收运费，单位元，历史未知为NULL',
 ADD COLUMN delivery_discount_price DECIMAL(19,2) NULL COMMENT '分摊至本行的运费优惠，单位元，历史未知为NULL',
 ADD COLUMN adjustment_amount DECIMAL(19,2) NULL COMMENT '本行人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
 ADD COLUMN use_point BIGINT NULL COMMENT '分摊至本行的使用积分数量，历史未知为NULL',
 ADD COLUMN give_point BIGINT NULL COMMENT '分摊至本行的赠送积分数量，历史未知为NULL',
 ADD COLUMN refunded_quantity INT NULL COMMENT '已成功退款数量，售后首次成功同事务增加，历史未知为NULL',
 ADD CONSTRAINT chk_trade_item_discount CHECK(discount_amount IS NULL OR (discount_amount>=0 AND discount_amount<=line_amount)),
 ADD CONSTRAINT chk_trade_item_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price)),
 ADD CONSTRAINT chk_trade_item_points CHECK((use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0)),
 ADD CONSTRAINT chk_trade_item_refund_quantity CHECK(
   refunded_quantity IS NULL OR (refunded_quantity>=0 AND refunded_quantity<=quantity));

ALTER TABLE trade_after_sale
 ADD COLUMN refund_discount_amount DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次商品优惠归因，非额外退现金，单位元',
 ADD COLUMN refund_delivery_discount_price DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次运费优惠归因，非额外退现金，单位元',
 ADD COLUMN return_use_point BIGINT NULL COMMENT '生成退单时冻结的应返使用积分，实际到账以账户流水为准',
 ADD COLUMN deduct_give_point BIGINT NULL COMMENT '生成退单时冻结的应回收赠送积分，实际扣回以账户流水为准',
 ADD COLUMN refund_rule_version INT NULL COMMENT '退款分摊规则版本，1为现金单件基准末次尾差及积分累计比例，历史未知为NULL',
 ADD CONSTRAINT chk_trade_after_sale_promotion_amount CHECK(
   (refund_discount_amount IS NULL OR refund_discount_amount>=0) AND
   (refund_delivery_discount_price IS NULL OR refund_delivery_discount_price>=0)),
 ADD CONSTRAINT chk_trade_after_sale_points CHECK(
   (return_use_point IS NULL OR return_use_point>=0) AND (deduct_give_point IS NULL OR deduct_give_point>=0));

-- 为既有可靠任务补充可恢复领取与单调版本，沿用原状态和重试次数。
ALTER TABLE trade_pay_notify_task
 ADD COLUMN execute_version BIGINT NOT NULL DEFAULT 0 COMMENT '单调执行版本，领取和人工重置均递增，完成时按版本CAS',
 ADD COLUMN lease_until DATETIME NULL COMMENT '当前执行租约到期时间，使用数据库时间，过期允许重新领取';

CREATE TABLE trade_order_promotion (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 order_id BIGINT NOT NULL COMMENT '所属交易订单主键编号',
 promotion_key VARCHAR(96) NOT NULL COMMENT '订单内稳定优惠标识，用于重放校验与去重',
 promotion_type VARCHAR(32) NOT NULL COMMENT '优惠来源：SECKILL、COMBINATION、BARGAIN、POINT_EXCHANGE、DISCOUNT、REWARD、COUPON、POINT_DEDUCT、MEMBER_LEVEL、ORDER_GIFT',
 source_id BIGINT NULL COMMENT '来源活动、会员券或等级等编号，按优惠类型解释，无来源编号时为NULL',
 promotion_name VARCHAR(200) NULL COMMENT '下单时优惠名称快照，历史未知为NULL，不读取当前名称回填',
 rule_summary VARCHAR(500) NULL COMMENT '下单时优惠规则展示摘要，历史未知为NULL',
 rule_snapshot LONGTEXT NULL COMMENT '实际使用的计算因子JSON快照，仅保存核对和支付前调价需要的规则',
 discount_amount DECIMAL(19,2) NULL COMMENT '该项商品优惠金额，单位元，历史未知为NULL',
 delivery_discount_price DECIMAL(19,2) NULL COMMENT '该项运费优惠金额，单位元，历史未知为NULL',
 use_point BIGINT NULL COMMENT '该项使用积分数量，历史未知为NULL',
 give_point BIGINT NULL COMMENT '该项赠送积分数量，历史未知为NULL',
 give_coupon_template_counts LONGTEXT NULL COMMENT '该来源赠券模板编号与数量JSON映射，已知无赠券为{}，历史未知为NULL',
 create_by VARCHAR(64) NULL COMMENT '创建人',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人',
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id),
 UNIQUE KEY uk_trade_order_promotion_key(order_id,promotion_key),
 CONSTRAINT fk_trade_order_promotion_order FOREIGN KEY(order_id) REFERENCES trade_order(id),
 CONSTRAINT chk_trade_order_promotion_amount CHECK(
   (discount_amount IS NULL OR discount_amount>=0) AND (delivery_discount_price IS NULL OR delivery_discount_price>=0)),
 CONSTRAINT chk_trade_order_promotion_points CHECK(
   (use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单实际采用的促销与权益结果快照';

CREATE TABLE trade_order_item_promotion (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 order_promotion_id BIGINT NOT NULL COMMENT '原订单优惠结果主键编号',
 order_item_id BIGINT NOT NULL COMMENT '原订单商品明细主键编号，不使用SKU编号替代',
 discount_amount DECIMAL(19,2) NULL COMMENT '该优惠分摊至本行的商品优惠金额，单位元，历史未知为NULL',
 delivery_discount_price DECIMAL(19,2) NULL COMMENT '该优惠分摊至本行的运费优惠金额，单位元，历史未知为NULL',
 use_point BIGINT NULL COMMENT '该来源分摊至本行的使用积分数量，历史未知为NULL',
 give_point BIGINT NULL COMMENT '该来源分摊至本行的赠送积分数量，历史未知为NULL',
 create_by VARCHAR(64) NULL COMMENT '创建人',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人',
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id),
 UNIQUE KEY uk_trade_item_promotion(order_promotion_id,order_item_id),
 KEY idx_trade_item_promotion_item(order_item_id),
 CONSTRAINT fk_trade_item_promotion_header FOREIGN KEY(order_promotion_id) REFERENCES trade_order_promotion(id),
 CONSTRAINT fk_trade_item_promotion_item FOREIGN KEY(order_item_id) REFERENCES trade_order_item(id),
 CONSTRAINT chk_trade_item_promotion_amount CHECK(
   (discount_amount IS NULL OR discount_amount>=0) AND (delivery_discount_price IS NULL OR delivery_discount_price>=0)),
 CONSTRAINT chk_trade_item_promotion_points CHECK(
   (use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单商品明细优惠与权益分摊快照';

CREATE TABLE trade_order_promotion_refund (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 after_sale_id BIGINT NOT NULL COMMENT '本次售后主键编号，关联trade_after_sale.id',
 order_item_promotion_id BIGINT NOT NULL COMMENT '原商品明细优惠分摊主键编号',
 refund_discount_amount DECIMAL(19,2) NULL COMMENT '本次商品优惠回退归因，非额外退现金，单位元，历史未知为NULL',
 refund_delivery_discount_price DECIMAL(19,2) NULL COMMENT '本次运费优惠回退归因，非额外退现金，单位元，历史未知为NULL',
 return_use_point BIGINT NULL COMMENT '该来源本次应返使用积分，实际到账以账户流水为准',
 deduct_give_point BIGINT NULL COMMENT '该来源本次应回收赠送积分，实际扣回以账户流水为准',
 create_by VARCHAR(64) NULL COMMENT '创建人',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人',
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id),
 UNIQUE KEY uk_trade_promotion_refund(after_sale_id,order_item_promotion_id),
 KEY idx_trade_promotion_refund_source(order_item_promotion_id),
 CONSTRAINT fk_trade_promotion_refund_after_sale FOREIGN KEY(after_sale_id) REFERENCES trade_after_sale(id),
 CONSTRAINT fk_trade_promotion_refund_source FOREIGN KEY(order_item_promotion_id) REFERENCES trade_order_item_promotion(id),
 CONSTRAINT chk_trade_promotion_refund_amount CHECK(
   (refund_discount_amount IS NULL OR refund_discount_amount>=0) AND
   (refund_delivery_discount_price IS NULL OR refund_delivery_discount_price>=0)),
 CONSTRAINT chk_trade_promotion_refund_points CHECK(
   (return_use_point IS NULL OR return_use_point>=0) AND (deduct_give_point IS NULL OR deduct_give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易售后生成时冻结的原优惠回退归因与权益方案';

-- 跨行校验由订单锁内的领域规则完成：
-- 1. COMPLETE订单所有必要金额/积分/计数非NULL且满足头行求和与应付公式。
-- 2. 优惠头与商品行属于同一订单，售后与原优惠属于同一订单明细。
-- 3. 金额分摊、积分分摊与退款归因总额不超过原冻结结果。
-- 4. 同订单锁内按order_no查询无处理中退单才允许创建；不保存退款中数量。
--    已退计数与售后首次成功同事务更新，重复状态转换不更新计数。
-- 5. rule_snapshot及赠券映射必须是合法且满足领域结构的JSON。
-- 不把通用软删除用于资金事实；不添加跨库外键；不修改历史已生成退单金额。
