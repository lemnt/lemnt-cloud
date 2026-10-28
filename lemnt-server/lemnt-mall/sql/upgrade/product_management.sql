-- 已有目录库升级一次；新库V2已包含扩展状态，历史ID保留。
ALTER TABLE mall_spu DROP CHECK chk_mall_spu_status;
ALTER TABLE mall_spu ADD CONSTRAINT chk_mall_spu_status
 CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED'));
