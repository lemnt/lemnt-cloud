# 会员等级管理收敛设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接108。核对源MemberLevelServiceImpl/ListReqVO/Mapper。当前名称/等级唯一及经验严格递增已匹配；补名称包含和状态筛选，修正后台等级/分组/标签共用元数据API旧路径。

独立MemberLevelListReq接name/status，Qry旧enabledOnly构造保留；简单启用列表及App仍强制启用，管理列表按名称/状态Lambda筛选、等级升序。后台接口/growth/levels不改，其余元数据实际/groups、/tags，仅同步前端映射，不改其业务。状态0必须有效。

等级DO继承本地BaseDO，新/旧库补可空审计，历史NULL；icon/backgroundUrl允许显式清空，使用ALWAYS更新策略保持原行为。Mapper简单读写直接BaseMapper，移除重复CRUD；原跨表NOT EXISTS保护删除SQL转XML并含等级resultMap。删除在基础配置事务锁内先查存在，不存在404、被使用409，不再返回含糊false；保留原子跨表删除保护，不弱化为先数后删。现有会员分配竞争边界不在本批扩展，不宣称解决全部并发分配/删除。

UI等级页开放名称/状态筛选并纳入缓存键，其他元数据不借用等级筛选。路径映射/growth/levels、/groups、/tags，修正文档网关示例但不应用Nacos。Gateway成员仍按表，复杂XML含resultMap，无JPA/注解SQL。

验收H2筛选0/组合/简单启用列表、BaseMapper字段清空及审计、真实旧表升级未知NULL、删除未存在/被使用/历史快照保留；MockMvc独立Req绑定/验证；前端路径/参数隔离及IDCRUD。完整四服务verify、UI检查、小程序回归，不commit、不执行真实库。
