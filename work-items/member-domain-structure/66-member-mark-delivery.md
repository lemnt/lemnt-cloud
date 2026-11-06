# 会员后台备注交付

2026-10-29，承接65设计。按源MemberUserDO.mark与测试DDL varchar(255)补member_user.mark，domain/DO/DTO、web独立Req/Res及后台表单/详情贯通。省略/null保留、空串清除，与资料同事务；App响应及更新契约、登录client DTO不增加备注，会员正常更新不覆盖后台备注。

前端原/member/admin/users与当前/member-users控制器不一致，本批同步修正资料和其关联操作路径，并更新请求契约测试。外部网关路由未发布，需部署转发现有控制器前缀。

相关Maven verify通过：member28、auth13、mall118、trade158，失败/错误/跳过0。源长度校正为255后再次member verify通过。新增存储用例验证备注写入、普通资料更新保留、超长拒绝、空串清除及App结构边界。React184项通过，类型/代码/格式检查及构建通过；新增前端请求和解码用例。后台路由更正后再次前端回归通过。

升级脚本仅生成未执行，历史备注NULL不回填；未做部署HTTP/浏览器验收，小程序未改动未重跑，未commit/push。会员真实姓名、地区、注册及登录审计等源差异仍待后续迁移；本批不宣称会员完整对齐。文章继续排除。

接口docs/member-profile-mark-api.md；证据evidence/member-mark-verification.json。
