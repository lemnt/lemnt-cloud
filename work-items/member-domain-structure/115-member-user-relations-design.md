# 后台会员关联信息返显设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接114。源MemberUserRespVO声明tagIds/tagNames/levelName/groupName，源分页批量读取关系后返显，详情现仅补levelName。按声明字段与现有关联表贯通后台列表/详情，不新增存储列。

MemberDTO追加可空关系投影，旧构造保持未知NULL；仅后台专用MemberUserPageQueryExe/DetailQueryExe调用批量关系服务，原MemberPageQueryExe/DetailQueryExe保持基础查询，App仍使用原Res，不增加内部备注/关系字段或额外关联查询。后台独立PageRes/DetailRes平铺新增tagIds/tagNames/levelName/groupName，统一分页保持。

按页批量读既有tag_link，再按去重ID各批读标签/等级/分组，最多4次关联读取，不逐会员N+1。各表Gateway用BaseMapper Lambda，不增重复Mapper CRUD。空页不查关系，缺失名称保持null/只返回已读名称；标签编号来自真实绑定，无绑定返回空列表，不猜名称或按序号推断。名称是当前元数据，不是等级变更历史快照。

React独立解码可选关系字段，列表/详情展示服务返回的名称和标签；缺名称保留编号提示，标签未读取与已知无标签区分。编辑请求仍白名单，不提交显示名称。身份、认证、密码和资料更新流程不改；注册IP/总积分等其他源字段不在本批伪造。

验收H2分页/详情关系一致、改名后读当前名称、清空关系、未知会员、基础DTO不被变异；MockMvc返回平铺字段及App不暴露；单测验证空页0读取及多会员每表一次批量；React关系解码/保存白名单。完整四服务verify、UI/小程序回归，不commit，不部署，无DDL。
