# 分组标签管理收敛交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接111；源Group/Tag PageReqVO、服务及Mapper。

两份分页Req从growth归回user域，去无效memberId和标签status，独立Qry/Filter补创建时间闭区间/单边与逆序校验。实际分页统一/groups/page、/tags/page，权限与统一rows/total保持。注册测试发现标签原类根路径误为/groups导致冲突，已明确改/tags并更新golden；原重复资源路径移除，根列表供选择器保留。

GroupDO/TagDO改BaseDO，模型/DTO及管理列表/详情/分页独立Res贯通可空createTime，简单列表不新增字段；未知历史不补时间。简单CRUD直接BaseMapper、备注ALWAYS保留NULL清空，分页和count共用Lambda。跨表未使用删除保护从注解移至含resultMap的XML；使用中409、缺失404，原外键/唯一键保留。数据库名称冲突转换409，不覆盖原行；源分组唯一约束因缺会员DDL未擅自删除，保留为待核对差异。

React分组/标签页面接入各自独立分页API，不再把所有行当数组展示；筛选/页码入缓存键，名称与创建时间可查、状态0仅分组可查，显示真实创建时间。等级页面继续数组列表，根选择器调用不变。新V2和group_tag_audit_fields.sql同步，H2保留旧唯一/外键结构执行升级通过，真实库未执行。

member/auth/mall/trade离线verify通过：89/18/119/158共384项，失败/错误/跳过0。新增3项H2集成测试覆盖两种分页/count/时间端点/单边/历史NULL/状态0、备注清空与冲突审计、真实旧结构升级；新增1项MockMvc验证独立请求/路径及参数隔离、Group状态422；已有引用归属/使用中拒绝删除与解除后删除用例继续通过。全量路由注册验证无冲突。

React216项通过，build/lint/format:check/typecheck通过，构建只有既有大chunk提示；新增请求/解码测试验证各自白名单、状态0/total及未知时间不补造。初次前端测试发现重复导入，已移除后全套通过。小程序123项回归通过。git diff --check通过。接口docs/member-group-tag-api.md；证据evidence/group-tag-management-verification.json及/tmp/group-tag-*.log。

未commit/push，未执行真实数据库/Nacos/部署HTTP/浏览器E2E/微信验收。源分组名称唯一、逻辑删除等仍待核对；不宣称完整会员迁移，文章排除。
