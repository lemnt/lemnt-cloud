# 后台会员关系返显交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接115；源MemberUserRespVO、UserController分页及Convert批量返显。

后台专用MemberUserPageQueryExe/DetailQueryExe包装原基础查询，补tagIds/tagNames/levelName/groupName平铺到独立PageRes/DetailRes，原路径、权限及统一分页不变。MemberDTO旧构造关系未知NULL，基础/App查询不做关联查询，App Res不新增字段。详情统一VO已声明关系，源详情实现当前仅补等级名称，本批补全后台详情关联展示；注册IP/总积分等不推算。

按页批量读既有tag_link，再按去重ID分别批量读标签/等级/分组，Gateway每表BaseMapper Lambda，一张关联表最多一次，空页0读取。没有新增名称存储列、N+1或Mapper重复CRUD。无绑定[]，名称缺失不猜，保留真实ID。基础DTO复制附加关系不变异，名称取当前元数据，不改历史等级快照。

React列表/详情显示服务返回的等级、分组名和标签；缺名称保留ID提示，未知与已知无标签分开。解码可选字段兼容旧响应，资料编辑仍白名单不提交关系返显。无DDL、RPC认证或密码逻辑改动。

member/auth/mall/trade完整verify通过，整理import及类型格式后member verify通过；最终98/18/119/158共393项，失败/错误/跳过0。新增1项H2贯通真实分页/详情、当前改名、清空绑定、未知会员及原DTO不变、历史等级描述保持；3项单测验证多会员每表一次、空页0查询、元数据缺失保留ID；1项MockMvc验证后台平铺列表/详情及App即便拿到同DTO也不返回后台新增字段。

React221项通过，build/lint/format:check/typecheck通过，构建仅既有大chunk提示；新增解码/请求测试验证真实名称、大ID、未知与空数组、资料编辑不回写关系。小程序123项回归通过。git diff --check通过。接口docs/member-user-relations-api.md；证据evidence/member-user-relations-verification.json及/tmp/member-user-relations-*.log。

未commit/push，未执行真实数据库/Nacos/部署HTTP/浏览器E2E/微信验收；不把MockMvc字段隔离视为网关权限E2E。其他会员字段/源最终契约仍待收敛，文章排除。
