# 签到执行规则交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接103；核对本地源MemberSignInRecordServiceImpl/Convert。

当天重复签到按源拒绝，统一ServiceException返回409“今日已签到”，不再返回旧成功。会员锁/日期唯一约束保留；并发一次成功、另一次409，签到/奖励仅写一次。拒绝不会更新旧时间。网关使用BaseMapper.insert生成真实记录ID，返回给应用，两类签到奖励bizNo改用签到记录ID字符串，旧日期键流水不改写。签到、积分、经验和升级继续同事务，奖励失败整体回滚。

汇总连续天数仅当最后业务日期为今天/昨天时有效，未来/更早记录归0；保留已知业务日期兼容旧记录，不推算旧事件时间。周期最大启用配置、断签归1、缺当前启用规则零奖励原逻辑与源一致，本批新增实际业务回归覆盖。无启用规则继续明确409，未复制源空配置空指针。

member/auth/mall/trade离线verify通过：69/18/118/158共363项，失败/错误/跳过0。更新旧重复成功断言为409，并发测试核验仅一次成功及明确409；奖励测试读回实际签到ID对应的积分/经验流水，时间拒绝后保持。新增3项H2集成测试覆盖周期上限和停用高天数/断签、停用当天规则零奖励和全停用拒绝、汇总四种日期边界。原经验奖励失败回滚签到及积分用例继续通过。

本批没有前端、小程序、SQL或HTTP字段变动，未重跑其检查；外部重复签到行为改为409，已写入docs/member-sign-record-api.md。git diff --check通过。证据evidence/sign-flow-verification.json，原始日志/tmp/sign-flow-final-verify.log。

未commit/push，未执行真实数据库/部署HTTP/微信验收。签到规则按day覆盖的管理表与契约仍不同于源ID CRUD，下一批继续；源deleted和业务日期兼容等未完成，不宣称完整签到迁移。文章排除。
