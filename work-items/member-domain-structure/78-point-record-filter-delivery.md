# 后台积分流水筛选交付

2026-10-30，承接77设计。独立积分Req将未生效name/status替换为nickname和bizType，与memberId组合；字符串空白忽略、业务类型精确匹配。Asset领域/DTO/DO附加当前昵称，后台积分分页Res输出，经验和App独立Res不增加。昵称关联会员当前资料，非历史快照。

关联查询迁XML并配置完整resultMap，分页与count共用条件且固定资产类型。前端积分页增加昵称/业务类型输入，修正共享列配置关闭了签到昵称搜索的问题；无效数值/原因/时间搜索继续保持关闭。只改查询和显示，不改积分写入、余额及幂等逻辑，不新增表或DDL。

member-start及依赖verify通过，会员35项失败/错误/跳过0。新增生产H2/XML用例覆盖总数/页大小、会员昵称业务类型组合、无匹配、经验隔离与查询不改余额；standalone HTTP用例验证参数绑定、当前昵称输出、超长类型422，不代替部署权限测试。React193项、类型/代码/格式检查及构建通过。

源bizType数值枚举仍未替换当前字符串，title与reason不等义未映射，源积分表完整字段/划分另行处理。未真实数据库/部署HTTP/浏览器验收，小程序未改动未重跑，未commit/push，文章排除。

接口docs/member-point-record-filter-api.md；证据evidence/point-record-filter-verification.json。
