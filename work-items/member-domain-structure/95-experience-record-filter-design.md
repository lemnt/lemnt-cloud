# 经验流水后台筛选设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接94。依据本地芋道MemberExperienceRecordPageReqVO与Mapper：会员编号、业务编号、数值业务类型、标题精确匹配，创建时间闭区间；分页按编号倒序。源类型0/1/4/5/11/12/13全部可查询，不为尚无写入路径的类型生成记录。

保持GET /member/admin/experience-records、权限member:experience:query及统一分页、独立Res。独立Req移除未生效name/status，保留memberId/pageNum/pageSize，增加bizNo（映射biz_id）、experienceBizType（映射biz_type）、title、startTime/endTime（yyyy-MM-dd HH:mm:ss，支持单边，闭区间，逆序拒绝）。新增专用ExperienceRecordAllPageQry/执行器，保留泛化资产查询仅供现有调用兼容；领域分页条件追加可空经验筛选，旧构造默认NULL。复用现有带昵称关联的经验XML/resultMap，page/count共用条件，不另加SQL接口。

后台React经验页增加源类型枚举选择、编号、标题与时间范围筛选，会员ID照旧；参数仅发送经验接口，0不能被真值判定丢弃。原业务码与数值源类型不混用；未知历史值保持NULL不推断。不变更数据库、App接口、认证或源业务写入。

验收：真实H2组合过滤、精确标题、类型0、历史NULL、时间两端/单边、翻页count一致、逆序/非法类型拒绝；MockMvc绑定真实独立Req、响应/验证；前端请求参数与跨记录类型隔离、业务类型0；全后端verify和UI构建/检查。未执行真实部署HTTP/数据库，不commit。
