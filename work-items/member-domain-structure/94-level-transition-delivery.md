# 等级自动变更规则交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer；承接93及91/92等级快照。

MemberGrowthService按芋道addExperience/calculateNewLevel对齐：经验超额扣减余额归零，流水保留请求扣减量；仅匹配到不同启用等级时更新等级并写快照。无匹配或无启用配置保留原等级，匹配低等级可降级，同等级经验变化不新增等级记录。明确手工取消仍可清空。积分不足的现有校验、会员锁、事务和幂等内容核对保留。

新增4项真实H2测试：低于最低门槛保留等级；仅停用配置保留；从高等级降到匹配低等级并核对余额/门槛快照，同等级不重复记录；无等级配置时经验超额扣至零、保留原扣减值并幂等重放、不同请求内容409及积分不足400。已有账本失败回滚、签到及交易权益测试继续通过。

离线Maven verify通过：member50/auth18/mall118/trade158，共344项，失败/错误/跳过0。证据evidence/level-transition-verification.json，原始日志/tmp/level-transition-verify.log。代码检查git diff --check通过。前端/小程序本批无改动，未重复运行其验证；先前91/92快照展示证据仍单独保留。

无表结构或HTTP字段变动，未执行真实数据库/部署HTTP，未commit/push。源零变更直接返回与现有拒绝零变更，以及退款入口既有可退余额裁剪仍待后续对齐；不宣称会员完整迁移完成。下一步继续F04经验流水查询及源接口收敛，文章排除。
