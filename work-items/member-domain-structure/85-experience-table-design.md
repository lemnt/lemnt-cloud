# 经验独立表设计

2026-10-30，承接84。新增member_experience_record，源user_id/biz_id/biz_type/title/description/experience/total_experience/create_time，保留biz_code/reason兼容原业务键与原因，数值BIGINT不截断long。独立DO/Mapper/XML resultMap/Gateway/Converter，原资产Gateway仅路由POINT/EXPERIENCE，不再持有旧表Mapper。移除本轮替代后孤立的旧AssetRecordDO/Mapper/Converter/XML，新库不再建member_asset_record；旧库复制脚本保留旧表作归档。

经验源映射：ADMIN/ADMIN_LEVEL→0，SIGN_IN→4，TRADE_EXPERIENCE→11，TRADE_EXPERIENCE_RETURN（售后）→13。邀请码/抽奖/整单取消当前无独立来源不生成。新增experienceBizType，pointBizType继续只用于积分；title/description在写入时按各自源模板保存，旧未知保持NULL，不用reason补造。后台/本人经验独立Res及前端/小程序读写展示。兼容HTTP原业务码和资产应用契约保留，不称最终字段完全一致。

详情按类型+ID，原会员锁、签到跨表事务、升级及退款累计不变。旧库停写备份后新建V5，copy旧EXPERIENCE保留ID/键/原因，历史元数据不推断，脚本不删除旧数据。测试H2新表、同ID隔离、升级/签到回滚和copy重试，相关后端及前端/小程序回归。不执行真实库/commit/push。
