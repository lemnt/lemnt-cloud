-- 仅初始化缺失配置；旧库必须先复制旧表数据，不能先以默认值覆盖迁移入口。
INSERT INTO sys_config(config_name,config_key,config_value,config_type,remark)
SELECT '积分交易配置','member.point.config','{"pointTradeDeductEnable":false,"pointTradeDeductUnitPrice":0,"pointTradeDeductMaxPrice":0,"pointTradeGivePoint":0}','1','通过原业务配置页面维护'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='member.point.config');
INSERT INTO sys_config(config_name,config_key,config_value,config_type,remark)
SELECT '交易基础配置','trade.config','{"id":1,"afterSaleRefundReasons":[],"afterSaleReturnReasons":[],"deliveryExpressFreeEnabled":false,"deliveryExpressFreePrice":0,"deliveryPickUpEnabled":false,"brokerageEnabled":false,"brokerageEnabledCondition":1,"brokerageBindMode":1,"brokeragePosterUrls":[],"brokerageFirstPercent":0,"brokerageSecondPercent":0,"brokerageWithdrawMinPrice":0,"brokerageWithdrawFeePercent":0,"brokerageFrozenDays":0,"brokerageWithdrawTypes":[]}','1','通过原业务配置页面维护'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='trade.config');
