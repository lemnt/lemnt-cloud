# 积分独立表交付

2026-10-30，承接83设计。新增member_point_record，源业务列及数值biz_type独立，配套PointRecordDO/Converter、MemberPointRecordMapper/XML/resultMap及MemberPointRecordGateway。简单CRUD/Lambda不重复定义Mapper方法。biz_code/reason保留原幂等来源及原因，point/total_point保留BIGINT防止截断，未知历史枚举可空。

原应用资产Gateway暂作兼容路由；所有积分写入、原键查询、分页/count、详情、售后累计切新表，不双写或回读旧POINT。经验仍旧表，详情显式按assetType+ID，两表同ID分别返回自身记录；旧经验详情不读归档POINT。新库旧资产表约束只允许EXPERIENCE。

member/auth/mall/trade verify通过：member39/auth18/mall118/trade158，共333项失败/错误/跳过0。经验详情归档过滤加强后member再次verify通过。原积分并发幂等、余额溢出和插入失败回滚、签到跨两表原子性、交易售后回退继续通过；同ID隔离测试按新语义验证。新增实际H2复制脚本测试保留旧ID/NULL元数据/原因，原请求重试不重复余额且不删旧归档。前端和小程序未改动未重跑。

旧库升级仅生成：先停写备份，创建新表并核对前置元数据列，再运行复制脚本，核对行数/内容/自增序列后切代码。脚本不删旧数据；MySQL、自增行为与实际停写部署未验收，未执行真实DDL、未commit/push。

尚未完成：源BaseDO完整审计、源int与当前long边界最终选择、HTTP数值bizType最终契约、经验表命名/拆分及旧归档清理。兼容字段biz_code/reason和旧应用接口仍存在，不能称全模块完全照搬。文章继续排除。

接口docs/member-point-record-filter-api.md；证据evidence/point-table-verification.json。
