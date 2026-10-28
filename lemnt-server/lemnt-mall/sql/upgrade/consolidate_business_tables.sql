-- 旧自动转换脚本已停用：新活动不再包含发布版本、限次计数及整单折扣。
-- 不得对存量库执行新库初始化脚本；请按 docs/yudao-alignment-upgrade.md 停写、备份、演练。
-- 此文件仅为升级入口，不执行 DDL，避免把旧规则静默转换成不同计价语义。
-- 已采用 mall_product_* 表名的库：使用 alignment_business_columns.sql 补充业务字段。
-- 活动表若已存在，先归档旧活动表及其依赖表，再使用 V15__promotion_activity.sql 建新表。
-- 旧活动全部保留供核对，人工按新接口重新配置并确认生效时间；不能自动启用。
SELECT '请先阅读 docs/yudao-alignment-upgrade.md，按实际库版本选择升级步骤' AS upgrade_instruction;
