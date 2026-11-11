-- 配置复制：停配置写入、备份，先执行business_config_storage.sql，再执行本文件，最后执行默认种子。
-- 库名按实际环境调整；来源旧表不删除；目标key已存在时不覆盖，需人工核对后决定。
USE lemnt_system;
INSERT INTO sys_config(config_name,config_key,config_value,config_type,create_by,create_time,update_by,update_time,remark)
SELECT '积分交易配置','member.point.config',CONCAT('{','"pointTradeDeductEnable":',CASE WHEN point_trade_deduct_enable THEN 'true' ELSE 'false' END,',"pointTradeDeductUnitPrice":',point_trade_deduct_unit_price,',"pointTradeDeductMaxPrice":',point_trade_deduct_max_price,',"pointTradeGivePoint":',point_trade_give_point,'}'),'1',create_by,create_time,update_by,update_time,'从旧业务配置表迁入'
FROM lemnt_member.member_config WHERE id=(SELECT MIN(id) FROM lemnt_member.member_config)
AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='member.point.config');
INSERT INTO sys_config(config_name,config_key,config_value,config_type,create_by,create_time,update_by,update_time,remark)
SELECT '交易基础配置','trade.config',CONCAT('{','"id":',id,',"afterSaleRefundReasons":',after_sale_refund_reasons,',"afterSaleReturnReasons":',after_sale_return_reasons,',"deliveryExpressFreeEnabled":',CASE WHEN delivery_express_free_enabled THEN 'true' ELSE 'false' END,',"deliveryExpressFreePrice":',delivery_express_free_price,',"deliveryPickUpEnabled":',CASE WHEN delivery_pick_up_enabled THEN 'true' ELSE 'false' END,',"brokerageEnabled":',CASE WHEN brokerage_enabled THEN 'true' ELSE 'false' END,',"brokerageEnabledCondition":',brokerage_enabled_condition,',"brokerageBindMode":',brokerage_bind_mode,',"brokeragePosterUrls":',brokerage_poster_urls,',"brokerageFirstPercent":',brokerage_first_percent,',"brokerageSecondPercent":',brokerage_second_percent,',"brokerageWithdrawMinPrice":',brokerage_withdraw_min_price,',"brokerageWithdrawFeePercent":',brokerage_withdraw_fee_percent,',"brokerageFrozenDays":',brokerage_frozen_days,',"brokerageWithdrawTypes":',brokerage_withdraw_types,'}'),'1',create_by,create_time,update_by,update_time,'从旧业务配置表迁入'
FROM lemnt_trade.trade_config WHERE id=1
AND NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='trade.config');
