# 签到配置ID管理交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接105；依据源MemberSignInConfigDO/Service/Mapper。

规则改独立ID主键及唯一day；创建重复409，不覆盖原奖励；修改按ID并允许改天数，撞其他规则天数409；修改/删除缺失ID404。新增Create/Update独立Req/Cmd/Exe，移除原Save覆盖请求/命令/执行器。POST集合返回生成ID，PUT/{id}修改、GET/{id}详情、DELETE/{id}删除；旧无ID集合PUT移除。列表/详情各自Res增加ID，App启用规则Res保持原字段。详情改selectById，不再全表扫描。

member_sign_in_config复用本地BaseDO，Mapper只继承BaseMapper，查询用Lambda，移除注解upsert及重复CRUD包装。应用预查配合数据库day唯一约束，DuplicateKeyException转统一409兜底并发，不按day覆盖。旧表升级sign_config_id.sql在真实H2执行通过：改名改列、day主键转唯一、生成新定位ID，业务奖励/状态保留、历史审计未知NULL；这些ID不是源历史ID。真实MySQL未执行。

React创建/修改分流，rowKey和删除用真实ID，编辑允许改day；请求只发送四项规则内容，不允许指定创建ID。修正签到配置调用到实际/growths路径，新增member:sign-in-config:create权限并补可重跑菜单seed与前端控制；仅update权限不能新增。网关配置示例补/growths/**，未应用Nacos。接口/升级说明docs/member-sign-config-api.md。

member/auth/mall/trade完整verify通过：73/18/118/158共367项，失败/错误/跳过0；随后对格式/import整理及新增菜单断言重跑member verify通过。新增3项H2集成测试覆盖ID CRUD/重复冲突及缺失、直接并发唯一约束、一份真实旧表升级；新增1项MockMvc覆盖独立创建/更新/删除以及路径ID绑定。路由/权限golden更新，菜单seed重复运行两次核验create权限仅一条，全部既有签到奖励执行继续通过。

React208项通过，build/lint/format:check/typecheck通过，构建保留既有大chunk提示。新增API/解码/页面权限测试覆盖大ID字符串、四字段白名单、路径、未知ID不借day补造、create与update权限分离。构建发现Mutation创建ID/更新Boolean返回联合类型，已明确类型后全套检查通过。小程序契约未改，123项回归通过。git diff --check通过。证据evidence/sign-config-id-verification.json及/tmp/sign-config-id-*.log。

未commit/push，未执行真实数据库、菜单/角色授权或Nacos变更；部署HTTP/浏览器E2E/微信验收未运行。旧客户端需同步ID接口，已有库停写备份核对结构后一次升级，不能重跑V2初始化；签到记录升级脚本按库现状另行执行。源deleted、业务日期兼容、其他会员最终接口路径等仍待收敛，文章排除。
