# 同等级手工调整交付

2026-10-31，承接121。对照本地芋道MemberLevelServiceImpl.updateUserLevel，修复目标等级与会员当前等级相同时，错误重置经验、增加经验和等级流水的问题。

在原会员行锁、请求校验及已执行业务编号重放检查之后，Objects.equals比较levelId，相同（包括双方null）直接成功。保留原经验和流水，即使当前等级配置后来修改或停用也不触发重置。真实切换仍按原目标门槛更新和写快照；已有业务编号内容冲突仍409；无操作不写占位记录，不消耗新请求编号。

先用3项H2回归确认原实现失败：150经验被同等级提交重置为100，无等级再次取消导致50清零，并发同等级提交同样重置。修复后验证相同等级、配置修改停用、无等级取消、后续真实切换、并发提交均通过，旧幂等和快照等回归保留。未改已有历史流水或真实会员余额。

联合验证发现上一批RedisSessionOperationsTest使用默认MockMaker时出现mock handler断言失败；改为显式沿用项目现有subclass MockMaker，测试内容和断言不变，联合运行两项均通过。

common+member/auth/gateway/mall/trade联合verify通过：member108/auth20/mall119/trade158，共405业务测试，gateway34、common-security29，失败/错误/跳过0；网关禁止Dubbo构建规则继续通过。git diff --check通过。证据evidence/member-level-noop-verification.json，失败复现/tmp/member-level-noop-reproduction.log，完整通过/tmp/member-level-noop-verify.log。

额外核对积分累计字段：源MemberUserRespVO声明totalPoint，但MemberUserDO仍标记TODO，当前updatePointIncr只修改余额，未发现累计积分存储/更新实现，因此未增加字段或用余额、流水擅自推算累计积分。

接口补充docs/member-level-record-api.md。无HTTP字段/前端/小程序/DDL变化，本批不重复其测试。未commit/push，未真实MySQL/Redis/Dubbo/部署HTTP或浏览器E2E验收；完整迁移仍in_progress。
