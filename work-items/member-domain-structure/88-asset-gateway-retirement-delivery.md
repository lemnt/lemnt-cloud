# 泛化资产网关退役交付

2026-10-30，承接87设计。删除MemberAssetRecordGateway及MemberAssetRecordGatewayImpl。MemberGrowthService直接注入Point/Experience逐表网关，在已有应用编排内选择原请求、保存和退款累计；后台列表、本人列表及详情执行器直接选对应网关，不新建同义路由Service。

逐表save新增资产类型匹配校验，防止共享AssetRecord投影误写另一张表；详情拒绝非法类型。测试夹具直接使用真实两张表网关，无测试用泛化路由。HTTP独立Req/Res、原幂等键、会员行锁、签到跨表事务、升级及退款回退行为不变。

删除源文件后member/auth/mall/trade clean verify通过：member42/auth18/mall118/trade158，共336项失败/错误/跳过0。原并发幂等、余额回滚、签到双表、升级、退款累计、迁移复制与原请求重试继续通过。新增实际存储边界测试验证误路由写入拒绝且两表无记录、非法详情类型拒绝。源Java扫描无MemberAssetRecordGateway引用，git diff --check通过。

无DDL/新表/前端字段变化，前端和小程序本批未改动未重跑。未部署HTTP或真实数据库验收，未commit/push，文章排除。AssetRecord/Qry/DTO仍为应用兼容投影，源最终HTTP数值类型字段命名、审计基类列等仍待收敛；不称已完全复刻。

接口docs/member-point-record-filter-api.md；证据evidence/asset-gateway-retirement-verification.json。
