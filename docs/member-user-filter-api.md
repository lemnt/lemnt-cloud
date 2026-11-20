# 后台会员列表源筛选

2026-10-30，GET /member-users，权限member:user:query，独立MemberUserPageReq与原统一rows/total、资料Res不变。

|参数|语义|
|---|---|
|mobile/email/nickname|包含匹配，空白忽略，最多24/254/80字|
|levelId/groupId|可选正编号，精确匹配|
|tagIds|正编号列表，命中任一标签；空列表不筛选，重复编号去重|
|loginStartTime/loginEndTime|最后登录时间闭区间，可单边|
|createStartTime/createEndTime|注册时间闭区间，可单边|
|pageNum/pageSize|默认1/20，最大1000000/100|

时间格式yyyy-MM-dd HH:mm:ss，逆序返回400。手机号由原精确改源包含匹配；登录和密码查找仍精确，不复用管理筛选。标签通过既有member_tag_link参数化EXISTS+IN，命中多个标签不会重复会员；其他条件与标签条件AND组合，分页/count共用同一XML条件、按ID倒序。NULL登录时间不匹配非空时间范围，无虚构回填。

React使用独立MemberUserPageQuery，不扩展流水入参。元数据选择器受对应level/group/tag读取权限限制；标签ID保留字符串，Axios明确重复查询键（tagIds=9&tagIds=10），不做数字精度转换。页面展示已有真实注册/登录时间，条件及页码入原缓存键。元数据权限不足时不请求其列表，不从ID猜名称。

MemberUserMapper增加完整MemberMap，复杂分页/count、原数据库时间和行锁移XML，简单ID查询直接BaseMapper。密码及资料更新契约不变，内部映射字段仍经过既有资料DTO/独立Res转换，未新增密码返回字段。无新增表或列，无数据库升级脚本。网关示例补/member-users/**但未应用Nacos。H2、MockMvc和前端请求/实际Axios序列化测试不是部署HTTP或浏览器E2E验收。


后续116批次已在不改变分页封装的前提下新增后台关系返显字段，见[会员关系返显](member-user-relations-api.md)。本文件“不加返回字段”指114筛选批次当时范围。
