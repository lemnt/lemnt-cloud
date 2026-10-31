# 会员列表源筛选迁移设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接112。源MemberUserPageReqVO/Mapper支持手机号/邮箱/昵称包含、登录/创建时间、等级/分组和任一标签OR筛选，当前只有手机号精确。

独立MemberUserPageReq/Qry与领域MemberPageFilter接通全部条件，时间使用loginStartTime/loginEndTime/createStartTime/createEndTime（本地yyyy-MM-dd HH:mm:ss，闭区间、可单边，逆序拒绝）。会员/标签关联ID正数，标签列表去重且空列表不筛选；手机号最多24、邮箱254、昵称80。分页默认1/20，保持统一rows/total和既有独立Res，不加返回字段。

标签仍存既有member_tag_link，不复制源逗号标签字段；使用参数化EXISTS+IN表达任一标签，避免JOIN重复会员。复杂列表/count共用XML条件及完整MemberMap，按ID倒序；原注解数据库时间和行锁一并移XML，简单按ID读取直接BaseMapper，不改安全资料更新/密码接口。无表结构变化。

React使用独立MemberUserPageQuery，开放邮箱/昵称、元数据选择器和标签多选、两种时间范围；元数据加载受对应读取权限限制，标签数组保留字符串ID，并明确以重复键序列化。列展示现有真实时间，不补历史NULL。其他记录页面不共享用户筛选入参。

验收H2全组合、包含匹配、时间端点/单边、标签OR且多个命中不重复、空标签不筛选、未知登录NULL、分页/count和非法范围/ID；MockMvc独立Req/数组绑定与验证，前端参数白名单及数组实际序列化；完整四服务verify、UI及小程序回归。不commit、不部署，无真实库变更。
