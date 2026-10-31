-- 支持积分商城0元兑换：订单金额允许为0，普通订单仍由商城结算拒绝0元提交。
ALTER TABLE trade_order DROP CHECK chk_trade_order_total;
ALTER TABLE trade_order ADD CONSTRAINT chk_trade_order_total CHECK(total_amount>=0);
