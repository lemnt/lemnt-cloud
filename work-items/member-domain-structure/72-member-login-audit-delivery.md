# 会员成功登录时间交付

2026-10-29，承接71设计及F04。member_user新增可空login_date，原领域/DO/DTO及后台分页详情映射，详情显示最近成功登录。App及认证跨服务DTO不新增审计字段，普通资料更新不写该列，历史未知不按创建时间补造。

auth验证密码、创建原公共会话后，调用MemberUserClient.recordLogin写动作；member锁会员核验启用并用数据库时间更新。审计失败auth尝试注销刚建会话并返回失败，令牌创建失败不写审计，刷新/错误密码不写。auth仍无数据库，无新令牌组件或远程状态判断接口。不承诺跨服务全局事务或网络失败时注销必定成功。

相关Maven verify通过：member30、auth16、mall118、trade158，共322项，失败/错误/跳过0。新增会员真实H2时间持久化、禁用拒绝和更新保留；认证基于真实公共令牌组件与替换传输的测试覆盖成功一次、刷新及密码失败不写、签发失败不写、审计失败后缓存无新会话。React190项与类型/代码/格式检查、构建通过。测试中的接口事务代理类型声明已修正后完整回归通过。

升级脚本仅生成，未执行真实数据库；真实Redis/Dubbo网络、部署HTTP和浏览器未验收。未commit/push，小程序未改动未重跑。可信loginIp/registerIp及registerTerminal尚未迁移，不填客户端任意头或固定来源。文章继续排除。

接口docs/member-profile-mark-api.md；证据evidence/member-login-audit-verification.json。
