# 签到配置ID管理迁移设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接104。源MemberSignInConfigDO/Service以ID管理规则、day唯一，创建重复/修改撞天数拒绝，不以天数覆盖。

新库member_sign_in_config(id自增PK、day唯一、奖励BIGINT、status、本地BaseDO审计)，旧表member_sign_config改名/改列，原day主键改唯一并新增自增ID，历史新ID为升级生成定位键，不声称源历史ID；审计未知NULL。升级仅生成并用H2验证，真实MySQL需停写备份核对执行一次。状态继续项目1启用0停用，天数1..366及奖励至少一项正数保持现有校验，源deleted不引入。

管理接口/growths/signin-configs：POST独立CreateReq/Cmd/Exe返回生成ID，PUT/{id}独立UpdateReq/Cmd/Exe返回Boolean，GET/{id}详情，DELETE/{id}删除。旧无ID集合PUT移除。不存在修改/删除返回404，重复天数409；应用校验加数据库唯一约束兜底并发，捕获DuplicateKeyException转统一409。Mapper只BaseMapper，条件走Lambda，去掉注解upsert。详情不再全表扫描。列表/详情独立Res新增ID；App规则响应不新增内部字段。

创建权限member:sign-in-config:create独立于update，更新幂等菜单seed及映射测试。React区分create/update，记录ID作rowKey、删除ID、允许改day；列表解码要求真实ID，新增接口解码生成ID。同步修复签到配置前端旧/member/admin路径为实际/growths路径，不调整本批外其他配置接口。

验收H2创建重复/改day冲突/缺失ID/删除与签到执行、并发唯一性及旧结构升级保留day奖励状态和未知审计；MockMvc独立请求/路由/权限静态映射；React创建/更新/删除参数和ID解码、权限展示。完整后端verify与UI检查；App规则契约未改，小程序按需回归，不commit不部署。
