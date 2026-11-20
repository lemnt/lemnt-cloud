# 积分记录剩余筛选迁移设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接98。源后台MemberPointRecordPageReqVO/Mapper新增缺失的title包含匹配；源AppMemberPointRecordPageReqVO/Mapper和uniapp score.vue支持addStatus=true收入、false支出、未填全部，及创建时间闭区间。后台源无业务编号/时间筛选，不自行增加。

后台独立Req增加title（最多255字），现有应用分页Qry与领域Filter传递；原昵称、会员、数值类型及兼容业务码保留。React积分标题可搜索，经验标题继续精确匹配。

App独立PointRecordPageReq增加Boolean addStatus及startTime/endTime（yyyy-MM-dd HH:mm:ss，支持单边/闭区间，逆序拒绝）。现有内部资产Qry追加可空字段，旧构造保持；只有POINT可使用这些App筛选，会员身份仍来自网关上下文。共享领域条件新增addStatus，保留旧构造不附加条件。积分XML现有昵称关联不变，page/count共用条件，积分>0为收入/<0为支出，零变更只在全部可见。

小程序会员权益积分记录提供收入/支出切换和日期起止，日期转本地00:00:00/23:59:59，分页与刷新沿已应用条件；切换经验/签到清空积分筛选，服务白名单参数，禁止会员编号从筛选传入。响应及统一分页不变，不改积分写入、经验接口或SQL表。

验收H2后台标题与组合筛选、App正负零/时间端点、会员隔离/分页count、false与NULL不同、逆序/跨资产类型拒绝；MockMvc真实绑定与可信会员ID；React参数隔离；小程序service/page请求false、日期和分页保留。全后端verify、React构建检查与小程序Node测试，不commit，不部署。
