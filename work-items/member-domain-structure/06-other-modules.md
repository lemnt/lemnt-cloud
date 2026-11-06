# 其他模块结构与代码规范整理

> auth 的数据库设计已按用户约束修正，当前实现见 [auth 无数据库边界修正](07-auth-no-database.md)。下文保留上一轮整理与验证记录。

2026-10-27。按用户确认的范围，沿用 member 标准覆盖 auth、system、mall、trade、open 的各层。未提交或部署。

## 调整结果

| 模块 | 主要调整 | Controller | 按表 Gateway |
|---|---|---:|---:|
| auth | 身份、凭证、失败计数按表分开；默认密码编码器；认证服务只做编排 | 2 | 3 |
| system | 资料、密码、角色授权、公告阅读分开；角色/岗位关联独立存储；字典跨表更名由应用层保持事务 | 21 | 16 |
| mall | 分类、品牌、SPU、SKU、属性、库存调整、预占按表划分；商品组合和库存操作由应用层编排 | 8 | 7 |
| trade | 购物车、券模板、发券、结算、订单、发货、退款、活动各自入口；跨表事务归应用层；读取网关身份 | 19 | 25 |
| open | HTTP 类型与转换器归 web/index；应用输出改 DTO，移除 HTTP 注解，统一查询执行器命名 | 1 | 0 |

HTTP Adapter 使用 app/web，其下为业务域。system 原有操作审计 MQ 消费者保持在 adapter/mq/log，作为非 HTTP 消息入口。Redis、密码编码和 RPC 等非数据库 Gateway 保留各自职责。

- 混合 Controller 和通用多业务 Executor 按业务拆分；类型移除 Admin 命名，原 URL、权限和 ADMIN 业务值保持。
- HTTP 入口使用独立 Req/Res；app/web 不相互依赖业务模型；清理因拆分失去消费者的转换方法和模型。
- Cmd/Qry/DTO 与 client record 补齐序列化。domain 不依赖 client；RPC 实现在 application/dubbo。应用层不直接依赖基础设施。
- 每个数据库 GatewayImpl 只注入对应表 Mapper。商品/属性、库存/预占、结算/券、促销版本/范围/计数/使用事件等跨表操作由 application 保持原事务、锁顺序、CAS、幂等和回滚。
- trade 移除 MemberSessionService、MemberSessionGateway 和本地 token 校验，接口读取 SecurityUtils 上下文；HTTP 测试通过实际 HeaderInterceptor 验证身份来源。
- 商品浏览、优惠券中心/钱包、订单/退款、活动列表统一 R.page(rows,total)，移除自定义 HTTP 分页包装。后台和小程序传输解码同步，页面内部 records/total 状态保持。

## 验证结果

Java 17 离线全量 lemnt-server clean verify 成功：

| 模块 | 测试数 | 失败/错误/跳过 |
|---|---:|---|
| auth | 11 | 0/0/0 |
| gateway | 32 | 0/0/0 |
| open | 1 | 0/0/0 |
| member | 22 | 0/0/0 |
| mall | 40 | 0/0/0 |
| trade | 196 | 0/0/0 |
| system | 131 | 0/0/0 |

共 433 项后端测试通过，包括认证/限流、网关认证域与可信身份头、member 回归、真实 MyBatis/H2 SQL、库存并发、结算/促销/支付/发货退款与事务回滚、system 权限和真实本地文件上传。清理构建排除了旧包名编译产物残留。

新增 Spring MVC 全量注册测试，逐条核对 auth 7、system 87、mall 23、trade 44、open 1，共 162 个 HTTP 操作的路径、方法和权限，且无重复映射；member 原有 67 个操作的测试继续通过。分页响应变化按本节明确记录。

147 条 SQL 注解展开常量后与开始时工作区快照一致：auth 8、mall 42、trade 97。本轮未修改 system 的 Mapper XML。19 个 client 类型的包名和方法签名保持。静态分层检查通过，gateway 包无 Dubbo、auth-client、member-client。

后台 169 项、小程序 69 项测试通过。后台 typecheck、lint、format:check、build 通过；git diff --check 通过。

验证边界：未做部署环境 MySQL/Redis/Nacos/Dubbo 联调、微信开发者工具编译、真机或浏览器人工验收；未执行数据库迁移。分页响应调整需要与已同步的前端一起发布。

证据：[后端清理构建](evidence/other-modules-clean-verify.log)、[测试统计](evidence/other-modules-test-counts.json)、[分层核对](evidence/other-modules-layer-audit.json)、[SQL 核对](evidence/other-modules-sql-audit.json)、[HTTP 快照](evidence/other-modules-http-routes.json)、[网关打包](evidence/other-modules-gateway-archive-audit.json)、[后台测试](evidence/other-modules-ui-tests.log)、[小程序测试](evidence/other-modules-miniapp-tests.log)。
