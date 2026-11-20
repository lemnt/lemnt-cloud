# 签到记录时间及筛选交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer；承接101，对照源MemberSignInRecordDO/PageReqVO/Mapper。

新库签到表改源member_sign_in_record、user_id/day，保留sign_date日期幂等键与BIGINT奖励。DO继承本地BaseDO，领域/DTO和四份独立HTTP Res贯通可空createTime。新签到取同一次数据库时间按DATETIME秒精度截断，确定日期和事件时间，初始updateTime一致；原请求返回旧记录不刷新审计。直接使用BaseMapper.insert，去掉重复插入封装。XML保留昵称关联、完整resultMap及审计时间列。

后台独立Req/Qry/领域Filter增加正day及时间闭区间/单边，逆序拒绝；page/count共用条件。React提供天数与时间筛选和时间展示，小程序仅显示已知时间。新字段没有向其他接口混传，App入参不增加后台筛选，原路径/权限/统一分页保持。docs/member-sign-record-api.md已更新。

真实旧结构升级脚本sign_record_time.sql在H2执行通过，改表/列名并保留ID/业务日期/天数/奖励，新增真实时间和操作者未知NULL；不按业务日期补时间。真实MySQL未执行，已有库须维护窗口停写备份核对约束后一次升级，不能重跑初始化V2。

离线member/auth/mall/trade verify通过：66/18/118/158共360项，失败/错误/跳过0。新增3项H2用例覆盖签到时间/重试/初始审计、天数+昵称+会员+时间组合及分页count/端点/单边/未知NULL、真实旧表升级；1项MockMvc绑定与非正天数422。原签到奖励事务回滚与幂等、交易权益继续通过。测试发现day关键字和DATETIME秒舍入问题，已分别转义和写入前统一精度，最终完整回归通过。

React204项通过，build/lint/format:check/typecheck通过，构建只有既有大chunk提示。小程序123项通过；新增前端/小程序测试验证未知时间不补造及参数隔离。git diff --check通过。证据evidence/sign-record-time-verification.json，日志/tmp/sign-record-time-source-verify.log和/tmp/sign-record-time-{ui-*,mini}.log。

未commit/push，未执行真实数据库/部署HTTP/浏览器E2E/微信验收。源deleted、sign_date兼容列和签到规则等源差异仍在；本批不宣称会员完整迁移，文章排除。
