-- MySQL 8.0.16+，从原 V10 券结构升级，维护窗口内停止券写入后执行一次。
-- 新库直接执行初始化 SQL，不要再执行本文件；此脚本未在真实数据库运行。
-- 先备份 mall_coupon_template、mall_coupon。历史券原始权益无法从已编辑过的模板恢复，
-- 回填以升级时模板现值为准，执行前须核对业务历史；上线新代码前完成回填。
ALTER TABLE mall_coupon_template
 DROP CHECK chk_trade_coupon_template_counts,
 DROP CHECK chk_trade_coupon_template_dates,
 MODIFY valid_from DATETIME NULL,
 MODIFY valid_until DATETIME NULL,
 MODIFY take_type VARCHAR(16) NOT NULL DEFAULT 'ADMIN',
 ADD description VARCHAR(500) NULL,
 ADD take_limit_count INT NOT NULL DEFAULT 1,
 ADD product_scope VARCHAR(20) NOT NULL DEFAULT 'ALL',
 ADD product_scope_values LONGTEXT NULL,
 ADD validity_type VARCHAR(16) NOT NULL DEFAULT 'DATE',
 ADD fixed_start_term INT NULL,
 ADD fixed_end_term INT NULL,
 ADD use_count INT NOT NULL DEFAULT 0,
 ADD deleted BOOLEAN NOT NULL DEFAULT FALSE,
 ADD create_by VARCHAR(64) NULL,
 ADD create_time DATETIME NULL,
 ADD update_by VARCHAR(64) NULL,
 ADD update_time DATETIME NULL;
UPDATE mall_coupon_template SET take_type='ADMIN' WHERE take_type='MANUAL';
ALTER TABLE mall_coupon
 DROP INDEX uk_trade_coupon_member_template,
 DROP CHECK chk_trade_coupon_status,
 ADD code VARCHAR(64) NULL,
 ADD name VARCHAR(500) NULL,
 ADD take_type VARCHAR(64) NULL,
 ADD kind VARCHAR(64) NULL,
 ADD threshold DECIMAL(19,2) NULL,
 ADD fixed_amount DECIMAL(19,2) NULL,
 ADD discount_percent INT NULL,
 ADD discount_limit DECIMAL(19,2) NULL,
 ADD product_scope VARCHAR(64) NULL,
 ADD product_scope_values LONGTEXT NULL,
 ADD valid_from DATETIME NULL,
 ADD valid_until DATETIME NULL,
 ADD use_time DATETIME NULL,
 ADD deleted BOOLEAN NOT NULL DEFAULT FALSE,
 ADD create_by VARCHAR(64) NULL,
 ADD create_time DATETIME NULL,
 ADD update_by VARCHAR(64) NULL,
 ADD update_time DATETIME NULL,
 ADD INDEX idx_coupon_member_template(member_id,template_id),
 ADD INDEX idx_coupon_expire(status,valid_until);
UPDATE mall_coupon c JOIN mall_coupon_template t ON t.id=c.template_id
 SET c.code=t.code,c.name=t.name,c.take_type=t.take_type,c.kind=t.kind,
 c.threshold=t.threshold,c.fixed_amount=t.fixed_amount,c.discount_percent=t.discount_percent,
 c.discount_limit=t.discount_limit,c.product_scope=t.product_scope,
 c.product_scope_values=t.product_scope_values,c.valid_from=t.valid_from,c.valid_until=t.valid_until;
-- 原券和模板缺少审计事实时新审计列保持NULL，不伪造创建和修改时间。
-- 不伪造旧券核销时间；历史 use_time 保持 NULL，后续真实核销由数据库时钟写入。
UPDATE mall_coupon SET status='EXPIRED' WHERE status='AVAILABLE' AND valid_until<=CURRENT_TIMESTAMP;
UPDATE mall_coupon_template t SET use_count=(SELECT COUNT(*) FROM mall_coupon c
 WHERE c.template_id=t.id AND c.status='USED' AND c.deleted=FALSE);
ALTER TABLE mall_coupon_template
 ADD CONSTRAINT chk_trade_coupon_template_counts CHECK((total_count=-1 OR total_count>0) AND issued_count>=0
 AND (take_type<>'USER' OR total_count=-1 OR issued_count<=total_count)),
 ADD CONSTRAINT chk_trade_coupon_template_dates CHECK((validity_type='DATE' AND valid_until>valid_from)
 OR (validity_type='TERM' AND fixed_start_term>=0 AND fixed_end_term>0));
ALTER TABLE mall_coupon ADD CONSTRAINT chk_trade_coupon_status CHECK(status IN ('AVAILABLE','LOCKED','USED','EXPIRED'));
-- 回退：允许一人多券后不能直接恢复旧唯一索引；需停写、备份并处理重复后按旧版本恢复。
