# 分组标签管理收敛设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接110。源Group/Tag PageReqVO有名称/创建时间，分组另有状态。当前时间缺失、页面未用分页、请求错放growth且含无效memberId/标签status，路径/groups/groups/page与/tags/tags/page重复。

两份Req归user/req并各自只保留合法筛选；Qry/Filter补可空start/end（本地yyyy-MM-dd HH:mm:ss，闭区间/单边/逆序拒绝）。分页路径统一/groups/page、/tags/page，不保留重复路径，权限和统一分页不变。既有根列表供选择器保留。

GroupDO/TagDO继承BaseDO，模型/DTO及分页/管理列表/详情各自Res附加可空createTime；简单列表不增加字段。历史时间未知NULL，升级只加审计列。Mapper简单CRUD直接BaseMapper，备注可显式清空；分页/count同一Lambda条件，跨表删除保护移XML含resultMap。不存在删除404、使用中409，名称唯一冲突统一409。源分组服务未显式唯一校验但本地源DDL缺少会员表，保留现有库约束，不仅凭服务代码删除约束。

React分组/标签管理使用各自分页API与入参，等级保留数组列表及等级筛选；名称/时间、分组状态0可用，条件/分页入缓存键，展示真实时间。日期未知不推算，选择器根列表不改。JSON响应与DTO独立，不让标签接收分组状态。

验收H2分页总数/时间边界/NULL/状态0、重复冲突、备注清空/审计、删除保护与历史绑定、旧表升级保留约束；MockMvc两份独立Req/新路径/验证；React参数白名单/分页契约/条件隔离。完整四服务verify、UI及小程序检查，不commit、不部署。
