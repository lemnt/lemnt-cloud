# 后台签到记录昵称查询

2026-10-29，对照芋道签到记录后台查询及昵称拼接，非部署实测。

GET `/member/admin/signin-records`，权限`member:signin:query`，既有分页R.rows/R.total不变。独立请求：memberId可选正编号、nickname可选最多80字包含匹配、pageNum默认1最大1000000、pageSize默认20最大100。nickname空白忽略，与memberId同时生效，无匹配空页。超长昵称HTTP422，应用校验400。

每项响应保留id/memberId/signDate/day/point/experience，增加可空nickname；读取member_user当前昵称，不是历史签到快照，不从会员ID猜名字。会员改名后后台显示新昵称。普通App/本人记录使用各自独立响应，不增加该后台字段。

原签到请求中未生效的通用name/status定义移除；新字段仅签到查询接收，前端不向积分/等级流水发送nickname。签到表格日期、周期和奖励为展示列，已移除原来没有对应服务端条件的搜索控件。分页和count使用同一XML关联条件与完整resultMap，没有额外会员IN列表或N+1请求。

没有表结构/写签到/奖励逻辑变更。部署路由及权限切面、浏览器尚未验收。


## 真实签到时间及天数筛选（2026-10-30）

后台独立Req新增day（可选正整数精确匹配）、startTime/endTime（yyyy-MM-dd HH:mm:ss，闭区间、可单边），与会员/昵称组合。逆序时间应用返回400，非正天数HTTP422；列表和count使用同一XML条件。React新增天数、时间范围筛选和真实签到时间展示；这些后台条件不发送到积分等其他接口。

签到表统一为源member_sign_in_record，member_id→user_id、signin_day→day（SQL及DO转义关键字）；保留sign_date和日期唯一键供既有签到幂等。DO复用本地BaseDO，增加真实create_time及审计列。新签到只取一次数据库now并截断到DATETIME秒精度，日期和事件时间来自同一次事件，初始updateTime相同；原请求重试保留原时间。积分与经验奖励、连续周期逻辑未改变。

四份独立签到Res增加可空createTime，App不增加后台昵称。小程序仅在真实时间存在时展示，旧记录仍显示已知业务日期。旧表升级脚本sql/upgrade/sign_record_time.sql仅生成，需停写备份核对结构/唯一键/外键后执行一次；表/列改名保留ID、日期、天数、奖励，新增时间与操作者全部保持未知NULL，不补当前时间、不拼午夜。新建库使用新版V2，不能重复执行旧库升级。

H2已验证真实旧结构升级及业务查询，但真实MySQL、部署HTTP、浏览器和微信开发者工具未验收。源deleted、sign_date兼容列及完整签到规则源逻辑仍待收敛，不能把本批时间查询当作全量签到迁移完成。
