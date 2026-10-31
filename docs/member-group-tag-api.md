# 会员分组与标签管理分页

2026-10-30，依据源Group/Tag PageReqVO及当前项目约定。独立分页路径GET `/groups/page`、GET `/tags/page`，权限分别member:group:query/tag:query，返回统一rows/total；旧重复资源分页路径不保留。根GET /groups、/tags列表仍供选择器使用，创建/修改/删除路径与权限不变。

|筛选|分组|标签|
|---|---|---|
|name|包含匹配，最多100字|包含匹配，最多100字|
|status|0停用/1启用，0有效|不定义/不使用|
|startTime/endTime|真实创建时间闭区间，可单边|真实创建时间闭区间，可单边|
|pageNum/pageSize|默认1/20，最大1000000/100|默认1/20，最大1000000/100|

时间格式yyyy-MM-dd HH:mm:ss，逆序应用400。无效memberId入参从两份Req移除，标签不接收状态筛选；请求已归adapter.web.user.req。管理列表、分页和详情各自Res增加可空createTime；简单列表不新增审计字段。未知时间不推算，带时间筛选自然不匹配NULL。分页按ID倒序，列表和count同一Lambda条件。

DO复用本地BaseDO，简单CRUD直接BaseMapper，备注NULL显式清空；跨表删除保护在含resultMap的XML，使用中409、缺失404。名称冲突按现有数据库唯一约束统一409并保留原行。源分组服务未显式校验名称唯一，但当前本地源SQL未提供会员表DDL，未凭服务代码删现有约束；这仍是待核对的源差异。

React分组、标签页使用各自分页API/白名单参数，名称/时间与分组状态筛选接入，条件和页码均入缓存键；等级页仍保留数组列表。展示真实创建时间，旧NULL不补造。根列表选择器调用不改。

新库V2补审计列，旧库核对缺失列后一次执行sql/upgrade/group_tag_audit_fields.sql，历史未知NULL。H2验证保留原外键/唯一键的升级、分页及删除保护；真实MySQL、Nacos、部署HTTP、浏览器/微信未验收，未commit。详见112交付及验证JSON。
