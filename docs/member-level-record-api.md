# 后台等级记录字段说明

2026-10-30。沿用现有等级记录列表、详情和会员下等级记录接口及各自独立Res，分页继续统一分页对象。

|字段|含义|
|---|---|
|experience|变更后会员经验，保持旧接口含义；数据库user_experience|
|recordExperience|源记录经验值，数据库experience；自动升级取目标等级门槛，手工调整取目标经验减原经验，手工取消取负原经验|
|level|变更时目标等级数值快照|
|discountPercent|变更时目标等级折扣快照|
|description|源描述快照；手工为管理员调整为：等级名称或管理员取消了等级，自动保持空|
|reason|原业务备注，数据库remark|

旧记录缺失快照保持NULL，前端不将其转成0或按当前等级配置推算。取消等级时等级/折扣为空。例：自动余额150、门槛100，则experience=150、recordExperience=100；手工从150调整到门槛100，则experience=100、recordExperience=-50。

内部审计字段不新增到HTTP响应。升级仅生成脚本，未执行真实数据库；已有库用sql/upgrade/level_record_snapshot.sql备份核对后执行一次，不能重跑V2初始化。自动计算按源规则：仅匹配到不同的启用等级时变更；无配置、无启用配置或低于最低门槛时保留原等级，不创建取消记录。匹配到低等级时允许降级，手工取消仍可清空等级。经验超额扣减余额归零，经验流水保留请求扣减值；积分不足仍拒绝，现有零变更与退款入口行为本批不调整。


## 等级记录筛选

2026-10-30补充。GET `/member/admin/level-records`，权限`member:level:query`。保留memberId、pageNum/pageSize，新增可选正levelId与startTime/endTime；日期格式`yyyy-MM-dd HH:mm:ss`，闭区间、可单边，逆序返回400。分页默认1/20、最大页大小100。原name/status未生效，已从独立Req删除。

levelId精确匹配记录自身level_id，未知编号返回空页，不读取当前等级配置；指定等级时不包含取消等级的NULL记录，未指定时仍可查看。列表按记录ID倒序，列表/总数共用同一Lambda条件。等级编号在前端保留字符串，不转成JavaScript浮点数。

后台等级页提供编号和日期范围筛选；经验/积分/签到请求不发送levelId，积分请求不带这两类流水的时间范围。Mapper直接使用BaseMapper.insert/selectById，会员内记录列表和计数复用同一筛选。未新增SQL/表、路由或权限；真实部署HTTP及浏览器E2E仍未验证。
