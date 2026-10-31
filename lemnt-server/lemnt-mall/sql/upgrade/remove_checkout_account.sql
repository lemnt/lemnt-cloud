-- 仅在所有商城实例已切换到基础包分布式锁后执行；不可与旧行锁版本混用。
-- 此表只有技术锁键，不包含账户余额或结算业务数据。
DROP TABLE IF EXISTS mall_checkout_account;
