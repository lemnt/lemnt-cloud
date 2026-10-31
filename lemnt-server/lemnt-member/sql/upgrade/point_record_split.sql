-- 未执行真实库。停写并备份后先创建V4新表，再执行一次复制。
-- 需已执行旧point_record_metadata升级；保留原ID和未知元数据，不套用现枚举。
-- 不删旧数据；核对行数/内容/自增后切代码，旧POINT仅留归档。
INSERT INTO member_point_record(id,user_id,biz_id,biz_type,title,description,point,total_point,biz_code,reason,create_time)
SELECT id,member_id,biz_no,point_biz_type,title,description,delta,balance,biz_type,reason,create_time
FROM member_asset_record WHERE asset_type='POINT';
