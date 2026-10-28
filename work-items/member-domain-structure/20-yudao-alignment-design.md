# 三模块按芋道收敛设计

日期：2026-10-28；版本 v1；责任：code-software-engineer。
输入：用户授权“按照芋道逻辑迁移，多出的先删掉”、docs/mall-trade-member-yudao-comparison.md；参考提交 8db9153bfe6bdc03baf93b74e244a2e74477c5c4。
模板参考：code-software-engineer/references/development-design.md、api-documentation.md、java.md；项目 AGENTS.md 优先。

## 目标与边界

现有 mall、trade、member 已确认的商品/评价、两类促销、优惠券、订单、售后、物流、支付与会员业务按芋道逻辑收敛。先移除额外和重复入口及不再被使用的实现，再补齐现有业务链路。秒杀、拼团、砍价、分销、装修、客服和钱包不自动加入本次范围。

保留用户此前明确约束：CQRS/DDD 与 system 风格、App/Web 分包、逐表 Gateway/Mapper/DO、MyBatis Plus/Lambda/XML/resultMap；认证在 auth/网关且不引入短信微信；mall 负责购物/结算/促销且结算写 Redis；trade 保留同步库存预占/券锁定及支付后核券、异步统一回滚；保留明确要求的独立物流单查询。实际数据库不执行删表或改表，不提交/推送。

## 实现任务及验收

| ID | 目标/主要路径 | 实施 | 验收/影响 |
|---|---|---|---|
| A1 | mall adapter web/cart、web/settlement、通用促销与旧商品 Controller | 删除额外后台会员购物车、后台代下单、通用促销兼容/旧基础商品入口；移除重复 add 最终数量入口 | 移除路由/前端调用/权限种子，保留 app 购物车与两类促销 |
| A2 | trade adapter web/fulfillment、web/order | 售后统一商品售后；删除旧整单申请和重复原始订单详情、后台代收货 | 按现有售后和履约详情更新页面/测试 |
| A3 | member adapter web/user/growth | 删除后台创建/重置密码、旧 members 入口及额外直接经验调整；资料、等级/积分与流水按芋道保留 | 前端不再调用删除 API；本人改密码仍保留 |
| A4 | mall promotion 各层及 SQL | 删除自定义促销版本/额度/请求记录机制，满减直接业务规则、折扣按 SKU 规则管理 | 管理状态/计价/订单应用/测试与参考相符，不留孤立模型与表 |
| A5 | mall settlement、trade 支付/售后联动、member 交易权益 | 活动、券、会员权益接入结算，支付/退款按规则联动库存与权益；订单调价等按现有管理需要收敛 | 金额分摊、重复调用、部分退款及跨会员测试 |
| A6 | mall 商品/SKU、member 额外字段/端点 | SKU 经 SPU 保存；移除独立库存调整账本及 memberNo 扩展，调用迁移到保留接口 | 主键与订单快照不丢，维护当前 SQL 和迁移说明 |
| A7 | lemnt-ui、lemnt-miniapp、sql 菜单及 docs | 清理引用并更新保留接口契约 | Node 测试/类型检查/构建及 Java clean verify；文档不冒充线上验证 |

依赖：A1/A2/A3 先明确保留入口，A4/A6 后 A5，最后 A7 统一回归。每次删除追踪执行器/对象/客户端调用，只删除因此变成无用的代码。

## API与数据约束

被移除的路由不提供兼容假响应；调用端改为已有保留接口。HTTP 每个端点保留独立 Req/Res，简单标识用路径参数，Controller 不直接接领域模型。商品写入为 SPU 携带 SKU 列表；购物车 quantity 保存最终数量。促销管理契约以参考代码具体字段为准，在对应 API 文档原位更新，不继续保留草稿/发布版本/自定义限次字段。

删除额外 SQL 表仅从新库初始化移除；已有库给出独立归档脚本并注明停写、备份、核对条件，不自动执行。Redis 保留用户要求的结算上下文，基础锁包覆盖必要事务。现有明确的库存/券消息补偿保留，不再引入 outbox。

## 验证与交付限制

先验证删除端点不可访问及剩余链路可编译，再补业务用例验证结算/支付/退款。按实际受影响模块测试，最后全后端、前端和小程序回归。真实支付/退款/物流需要具体接入配置，不能用模拟成功冒充实现。服务未部署、真实 MySQL/Redis 未执行时须分别说明。
