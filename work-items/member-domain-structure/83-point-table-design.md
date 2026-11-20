# 积分独立表迁移设计

2026-10-30，承接82。新增member_point_record按源字段user_id/biz_id/biz_type/title/description/point/total_point/create_time建模；biz_type为源数值，历史未知可空。保留biz_code和reason两列作为当前幂等字符串与原业务原因兼容，不丢数据；金额仍BIGINT，不把现有long截成源int。独立DO/Mapper/Gateway/Converter，复杂分页XML含resultMap。

既有资产Gateway暂作应用兼容路由，POINT交独立PointGateway，EXPERIENCE仍旧表；积分请求、保存、计数、分页、详情及售后累积全部走新表，不双写/不读旧POINT。详情必须带assetType，两表ID允许重叠，防止串读。现有HTTP形状不改。新库旧资产表只允许EXPERIENCE，交易与签到事务/会员锁不变，跨表签到奖励一起提交。

旧库先停写、备份、执行新表初始化和copy脚本，保留旧ID及原业务键，不按现规则回填历史元数据。copy脚本不删旧记录，需核对数量/内容/自增序列后切代码；旧POINT只作归档，去旧表清理/经验表最终命名后续处理。不运行真实脚本。测试真实H2新表、积分并发/回滚/签到跨表原子性、同ID积分经验隔离及退款回退；跨模块回归。不commit/push。
