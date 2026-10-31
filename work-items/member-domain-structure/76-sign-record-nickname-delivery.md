# 后台签到昵称筛选交付

2026-10-29，承接75设计。GET /member/admin/signin-records独立请求增加nickname，去掉原未生效通用name/status定义。支持当前昵称包含匹配、空白忽略，与memberId同时生效。返回独立后台Res增加nickname。来源是member_user当前昵称，不保存或冒充历史快照。

签到Mapper关联分页/总数改XML，共用筛选SQL且包含完整resultMap；避免加载全量匹配会员ID和逐条请求昵称。Domain/DTO/DO转换贯通，DO昵称标记非表字段，App响应仍独立不增加昵称。签到写入、奖励和幂等逻辑未改。

前端签到类型增加昵称搜索和展示，仅签到接口发送nickname，移除日期/周期/奖励这些未实现条件的搜索控件。权限、路径及分页形状保留。

member-start及依赖verify通过，会员33项失败/错误/跳过0。新增实际H2/XML用例验证总数/页大小、组合、无匹配、改名，以及standalone HTTP昵称绑定/422，未验证部署权限切面。React192项和类型/代码/格式检查、构建通过。无DDL或新依赖，未部署HTTP/浏览器验收，小程序未改动未重跑，未commit/push，文章排除。

接口docs/member-sign-record-api.md；证据evidence/sign-record-nickname-verification.json。
