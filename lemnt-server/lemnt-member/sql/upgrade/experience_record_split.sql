-- 未执行真实库。停写备份后先创建V5新表，复制一次并核验ID/键/自增；保留旧表归档。
-- 经验旧记录没有可靠源枚举/title快照，不按当前规则回填。
INSERT INTO member_experience_record(id,user_id,biz_id,biz_type,title,description,experience,total_experience,biz_code,reason,create_time)
SELECT id,member_id,biz_no,NULL,NULL,NULL,delta,balance,biz_type,reason,create_time
FROM member_asset_record WHERE asset_type='EXPERIENCE';
