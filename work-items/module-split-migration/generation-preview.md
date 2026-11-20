# 生成计划预览（已确认，执行中）

## 已确认的设计选择

1. 全部业务表按 product/promotion/order/pay 所属模块统一重命名。
2. pay仅发布现金支付/退款事实；order校验后发布 ORDER_PAID、AFTER_SALE_COMPLETED，驱动库存、券、积分等权益，包括零现金订单和售后。
3. 保持现有HTTP路径、字段、响应、权限、部分退款和尾差；system项目不调整，不自动执行数据库迁移，不commit/push。

## 具体写入范围

| 阶段 | 新建目录 | 原业务表归属 | 验证重点 |
|---|---|---:|---|
| P1 | `lemnt-server/lemnt-product/` | 10张 | 商品、spec、评价、库存预占/确认/回补 |
| P2 | `lemnt-server/lemnt-pay/` | 11张 | 支付/退款、钱包、充值、资金事件 |
| P3 | `lemnt-server/lemnt-promotion/` | 22张 | 活动、券、购物车、DIY/客服与资源锁 |
| P4 | `lemnt-server/lemnt-order/` | 21张 | 结算、订单/售后、履约、物流、自提、分佣、优惠和活动扩展 |
| P5 | 调整实际生效入口，验证后删除旧mall/trade | 原64表完整映射 | 网关、RPC、构建、SQL及全量回归 |

四模块均为 client/domain/application/infrastructure/adapter/start 六层，新增各自POM、启动配置、Dockerfile和README。member增加冻结权益消费及一张Inbox，原有会员业务不整体重构。product/pay/order各增加Outbox+Inbox，promotion仅Inbox；新消息表共8张。配置文件仅交付源模板，不向Nacos或数据库自动发布。

- **全部源文件→目标文件、操作、复杂度和依赖**：`file-manifest.md`（1,949条迁移/必要ACL副本条目）。
- **新增/修改/最终删除的构建、契约、消息、网关、工具及配置文件**：同一清单第二部分（235条，包含旧14个POM与2个SQL的最终删除）。
- **64张表的一对一归属及新名**：`table-mapping.md`。
- **313个原HTTP方法映射及归属**：`evidence/http-endpoints.json`，活动查询与下单按端点分拆。
- **跨上下文待改写的源码引用**：`evidence/cross-context-imports.json`。实际代码需再通过编译与禁止跨域实现依赖的检查，不能只做字符串替换。
- **完整15章设计与业务边界**：`design.md`。
- **任务顺序、DAG、依赖和验证条件**：`implementation-plan.md`。

命名示例：`trade_order → order_info`、`trade_order_item → order_item`；订单优惠快照为 `order_promotion/order_item_promotion/order_promotion_refund`；原mall活动订单扩展为 `order_activity/order_activity_item/order_activity_refund`，两组不混合。三条支付↔订单/售后跨库外键改由可信RPC校验，同库外键保留。

## 当前验证与状态

- 已执行当前源码架构检查：2,804个生产Java文件、56个Mapper XML、72个POM，0违规。这是迁移前基线。
- 已检查清单目标路径无冲突，64张表目标名不重复，313条原始方法/路径映射无冲突。
- 尚未写入四模块业务代码，尚未执行新模块测试或真实环境迁移。
- design处于in_progress；task/generate仍pending。用户确认预览后完成正式tasks并开始实际生成，不虚构前置完成状态。

## 确认范围

确认本预览即确认代码分析发现、完整15章设计、逐文件范围及五阶段任务顺序；随后按P1～P5持续完成代码、测试、证据和进度更新，无需逐层重复确认。业务/API/数据库范围发生实质新增时才另行核实。

cs-generate 的强制检查点要求：先展示文件清单并等待“确认/开始/继续”等明确信号，才可写入业务代码。因此当前只交付可审查的设计与生成预览。


执行记录：用户在本预览后再次明确调用 cs-generate；方案与文件范围已确认，开始按 tasks.md 实施。以上“待确认/尚未生成”段落为确认前基线，实际进度以 tasks.md 与 code-summary.md 为准。
