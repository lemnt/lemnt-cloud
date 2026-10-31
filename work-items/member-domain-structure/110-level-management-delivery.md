# 会员等级管理收敛交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接109；源MemberLevelServiceImpl/ListReqVO/Mapper。

新增独立等级列表Req及name/status查询，名称包含、状态0正确保留，Lambda筛选按等级升序；既有enabledOnly构造保留，App/简单列表仍强制启用。React等级页提供名称/状态搜索、查询条件入缓存键，分组标签不继承等级条件。元数据请求映射修正为/growth/levels、/groups、/tags，其他两类仅路径修复，未改其业务模型。网关示例更新但未应用Nacos。

等级Mapper去掉重复查询/insert/update包装，直接BaseMapper；跨表原子删除条件保留并从注解移至含LevelMap的XML。图片字段ALWAYS更新策略保留显式NULL清空行为。删除持基础配置事务锁，缺失404、被使用409而非false，原会员外键/NOT EXISTS保留，成功不删除历史等级快照。

等级DO改本地BaseDO审计，新V2补可空列，旧库level_audit_fields.sql只加列，未知历史NULL不回填。H2测试通过保留唯一键和会员外键的真实旧结构升级；最初测试尝试drop表被外键阻止，已改为保留原约束构造旧结构再执行升级。未删除或放松约束，真实MySQL未执行。

member/auth/mall/trade离线verify通过：85/18/119/158共380项，失败/错误/跳过0。新增3项H2覆盖管理筛选/启用列表、图片清空及审计、新旧升级、被使用/缺失删除与历史快照（相关断言分布于3项用例）；新增1项MockMvc绑定/状态422与简单列表不受status=0影响。既有等级唯一/经验门槛、自动等级、配置基础锁、交易权益回归继续通过。

React213项通过，build/lint/format:check/typecheck通过，构建仅既有大chunk提示；新增2项API测试验证状态0/参数隔离、真实三类路径和等级ID CRUD/NULL图片。小程序123项回归通过，App接口未改。git diff --check通过。接口docs/member-level-management-api.md；证据evidence/level-management-verification.json及/tmp/level-management-*.log。

未commit/push，未执行真实数据库/Nacos/部署HTTP/浏览器E2E/微信验收；不宣称解决全部并发分配/删除竞争。等级源deleted、创建时间前端展示及其他会员域最终契约仍待收敛，文章排除。
