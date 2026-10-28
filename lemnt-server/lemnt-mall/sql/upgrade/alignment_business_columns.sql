-- 仅用于已采用 mall_product_* 表名、但尚无 source_no 的存量库；停写并备份后在演练库执行。
ALTER TABLE mall_product_stock_reservation ADD COLUMN source_no VARCHAR(40) NULL COMMENT '退货库存对应的原订单号';
