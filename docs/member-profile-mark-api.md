# 会员后台备注

2026-10-29。参考源MemberUserDO.mark和会员后台更新资料；当前实现只补备注，不代表全部会员字段已对齐。

GET /member-users（权限member:user:query）的分页项及GET /member-users/{id}详情增加可空mark，历史未知不补造。PUT /member-users/{id}（member:user:update）在既有资料请求中新增可选mark，最多255字；省略或null保留原值，空字符串明确清除。字段校验422，应用长度校验400；与昵称等字段同事务，不改变余额或密码。响应仍为R<Boolean>。

后台资料表单及详情展示备注。前端资料及其积分/等级/分组/标签/地址关联操作统一使用当前控制器/member-users路径，替换旧/member/admin/users。部署网关须转发该现有控制器前缀；本批未发布外部路由。

App ProfileUpdateReq/ProfileRes及登录client DTO不增加mark，普通会员资料修改保留后台备注。服务端内部DTO有mark但App响应转换不输出它。

新库member_user增加mark VARCHAR(255) NULL；旧库脚本lemnt-member/sql/upgrade/member_mark.sql仅生成未执行。未部署HTTP或浏览器验收。

## 后台姓名及地区字段（2026-10-29补充）

现有GET /member-users及GET /member-users/{id}增加可空name、areaId；PUT资料请求增加可选name（最多30字）和areaId（正整数，最大2147483647）。省略或null保留旧值，姓名空串可清除；地区不以0表示清除。校验失败字段422或应用业务400，原值不变，成功仍R<Boolean>。权限和路径不变。

字段来自源MemberUserDO和后台BaseVO，SQL按源测试DDL name varchar(30)、area_id int。历史未知不补造；areaName尚无合法解析来源，未迁移，不以编号或固定文字假充地区名。App请求/响应及认证客户端不增加这两项，普通会员资料更新保留后台姓名地区。

后台编辑表单增加真实姓名和地区编号，详情显示原值；当前地区输入为编号，未迁移源地区选择器。旧库member_identity.sql仅生成未执行，部署验收及字典解析仍待完成。

## 地区树与后台选择（2026-10-29）

GET `/areas/tree`由system提供，要求登录。返回R.data为中国下省市区节点数组，节点{id:number,name:string,children:[]}；叶子children为空数组，不返回全球其他国家。本地源area.csv原样迁移，是参考项目的数据快照，不声明为实时最新行政区划，无新增地区表。

会员编辑改用地区树选择，编号保持原INT；详情由父子路径生成名称。地区加载中/失败/旧编号未匹配分别提示，不用编号或固定名称冒充。加载失败可重试，未加载成功禁止地区修改，其余资料可编辑。旧地区不被自动清空；会员服务端areaName派生和RPC仍未新增。

部署须将/areas/tree转发system并保留路径，未发布外部路由，未浏览器验收。此实现替代上一节“地区选择器未迁移”的历史状态；地区数据更新需要后续明确来源。

## 最近成功登录时间（2026-10-29）

后台分页/详情增加只读loginDate，格式沿用数据库本地时间；历史未知为NULL，不用createTime补造。后台详情显示“最近成功登录”，资料PUT不接受登录时间，App及认证DTO不增加该字段。

auth校验手机号密码并成功创建现有公共Redis会话后，通过MemberUserClient.recordLogin(memberId)调用member；member本地事务锁会员核验启用，用数据库当前时间写member_user.login_date。密码失败、锁定或令牌创建失败不写；刷新不刷新该时间。审计RPC失败，auth尝试注销刚建会话并返回失败，不作为完整登录成功返回。跨服务没有全局事务，网络异常/注销失败仍需运维核验，不承诺跨库原子提交。

此接口为私有服务写动作，不新增HTTP审计入口或远程isActive接口。旧库member_login_date.sql仅生成未执行，auth无数据库。当前未迁移loginIp/registerIp/registerTerminal：没有可信来源时不将客户端任意头、网关地址或固定终端当成真实会员来源。

## 登录连接IP（2026-10-29）

后台分页/详情增加只读loginIp（源表VARCHAR(50)）。与loginDate同次数据库更新，未知值NULL，不将上次IP留作本次结果。后台名称为“登录连接IP”，App及认证返回DTO不包含该字段，资料更新不能设置它。

网关AuthFilter清除外部X-Lemnt-Login-Ip，再从exchange.request.remoteAddress写入该内部头；缺连接地址时保持空。不会读取客户端X-Forwarded-For或X-Real-IP来推断终端。auth将内部头转为登录Cmd上下文，会话创建成功后通过现有RPC写member；member仅接受数值IP，拒绝域名，不进行任意主机解析。旧recordLogin(id)保持事务兼容且将本次IP记为未知。

边界：这是网关观察到的连接对端，不保证是终端公网IP。网关前有代理时会记录代理地址；若部署启用ForwardedHeaderTransformer/forward-headers-strategy改变remoteAddress，其信任规则必须由部署方明确配置。本批没有实现可信代理链。auth服务只允许内部网关访问，否则直接传内部头不可信；没有发布网络访问策略或线上代理配置。

旧库member_login_ip.sql仅生成未执行，不回填历史。注册IP与注册终端仍待合法来源，未迁移。
