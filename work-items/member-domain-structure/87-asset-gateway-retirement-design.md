# 泛化资产网关退役设计

2026-10-30，承接86。MemberAssetRecordGateway及Impl已不对应数据库表，仅转发POINT/EXPERIENCE，移除这两个遗留抽象，不另建同义路由Service。MemberGrowthService直接注入Point/Experience逐表Gateway，内部请求/保存/退款累计按已验证类型编排；三个资产查询执行器直接选对应网关，保留各自参数校验。

应用层既有AssetRecord/Qry/DTO暂保留，HTTP独立Req/Res和业务码兼容不改，会员锁、跨表事务和幂等不变。逐表save拒绝错误assetType，避免泛化投影被误写另一张表。测试夹具直接使用两张表网关，不新增测试路由替身。

验证现有并发、签到、升级、退款、迁移重试，新增错误资产类型写入拒绝和非法查询类型拒绝。删除源文件后clean verify避免旧class/XML残留；跨模块回归。无DDL/前端字段变化，不commit/push、不执行真实库。
