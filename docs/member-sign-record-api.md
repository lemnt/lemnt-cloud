# 后台签到记录昵称查询

2026-10-29，对照芋道签到记录后台查询及昵称拼接，非部署实测。

GET `/member/admin/signin-records`，权限`member:signin:query`，既有分页R.rows/R.total不变。独立请求：memberId可选正编号、nickname可选最多80字包含匹配、pageNum默认1最大1000000、pageSize默认20最大100。nickname空白忽略，与memberId同时生效，无匹配空页。超长昵称HTTP422，应用校验400。

每项响应保留id/memberId/signDate/day/point/experience，增加可空nickname；读取member_user当前昵称，不是历史签到快照，不从会员ID猜名字。会员改名后后台显示新昵称。普通App/本人记录使用各自独立响应，不增加该后台字段。

原签到请求中未生效的通用name/status定义移除；新字段仅签到查询接收，前端不向积分/等级流水发送nickname。签到表格日期、周期和奖励为展示列，已移除原来没有对应服务端条件的搜索控件。分页和count使用同一XML关联条件与完整resultMap，没有额外会员IN列表或N+1请求。

没有表结构/写签到/奖励逻辑变更。部署路由及权限切面、浏览器尚未验收。
