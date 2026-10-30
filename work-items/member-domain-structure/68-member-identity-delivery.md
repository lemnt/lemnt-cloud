# 会员后台姓名地区交付

2026-10-29，承接67设计，参考源MemberUserDO、后台BaseVO与源测试DDL。member_user新增name varchar(30)、area_id int，可空；domain/DO/DTO、web独立Req/Res及后台编辑/详情贯通。省略/null保留，姓名空串清除；地区正int。原资料更新同事务，超长或非法地区不写入。

App请求/响应及认证client契约不增加这两项；普通会员资料更新保留已有后台姓名/地区。历史未知不回填，地区名称不从编号猜测。当前后台地区仍为编号输入；源地区名称解析和选择器未完成，不能称完整资料交互迁移。

相关Maven verify通过：member29、auth13、mall118、trade158，失败/错误/跳过均0。新增真实H2存储用例验证姓名地区往返、会员更新保留、超长姓名/0地区拒绝且原值不变、姓名清除、App契约边界。React186项通过，类型/代码/格式检查与构建通过；新增原文空串及地区范围的请求/解码验证。

升级脚本仅生成未执行，未部署HTTP/浏览器验收；小程序未改动未重跑。未commit/push，文章继续排除。其他源会员资料和注册/登录审计字段仍待迁移。

接口docs/member-profile-mark-api.md；证据evidence/member-identity-verification.json。
