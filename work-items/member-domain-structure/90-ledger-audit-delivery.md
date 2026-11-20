# 流水基础审计交付

2026-10-30，承接89，沿system本地BaseDO与统一填充器。积分/经验DO继承BaseDO、去掉重复createTime，补create_by/update_by/update_time可空列及XML resultMap。新行操作者用现有安全上下文，缺失时沿统一组件system服务身份；初始更新时间采用账本同一数据库事件时间。原请求重试不更新审计。

旧库新增列及旧表copy保留未知NULL，不回填当前人/时刻。只生成ledger_audit_fields升级脚本，公共BaseDO/填充器未改，HTTP/DTO未新增内部字段，源deleted和流水删除能力未引入。

member/auth/mall/trade verify通过：member43/auth18/mall118/trade158，共337项失败/错误/跳过0。新增真实H2验证两表上下文操作者/一致事件时间、重试换操作者仍保留原审计；增强原copy用例核验旧行审计NULL。原并发、签到跨表事务、升级、退款及迁移重放继续通过。测试夹具SqlSessionTemplate改为字段供审计读回，修正编译后完整回归通过。

无前端或小程序改动，本批未重跑其检查。升级仅生成未执行，真实数据库/部署HTTP未验收，未commit/push，文章排除。源基类deleted、完整最终接口命名和应用兼容投影仍待收敛。

接口docs/member-point-record-filter-api.md；证据evidence/ledger-audit-verification.json。
