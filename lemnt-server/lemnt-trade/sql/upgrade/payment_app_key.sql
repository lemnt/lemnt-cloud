-- 存量库：停写备份并核对字段未存在后执行；支付单绑定发起支付的应用，历史支付单保留NULL。
ALTER TABLE trade_payment ADD COLUMN app_key VARCHAR(64) NULL AFTER channel;
ALTER TABLE trade_payment ADD KEY idx_trade_payment_app(app_key, channel);
