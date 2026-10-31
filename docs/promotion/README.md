# 促销引擎设计与实施

promotion-v1 · 2026-10-25（Asia/Shanghai）· 基础20ec7bd。

**T1计价、T2持久化及T3事实RPC/六个管理API已实现；购买链路尚未接入。** 实施与验证见[implementation.md](implementation.md)。[T2实施](persistence.md)；[T3实施](management.md)；T4..T7和五个V2购买HTTP入口仍待实现；现有优惠券、结算、支付和退款行为保持兼容。

| 文件 | 内容 |
| --- | --- |
| [design.md](design.md) | 首期规则、组合/互斥、整数分计算与分摊、缓存提单、活动占用/补偿、数据模型和T1..T7任务 |
| [api.md](api.md) | 11个拟新增管理/小程序入口、完整字段、权限、错误与构造示例 |
| [contracts](contracts) | 15份JSON Schema，包含自定义int64边界格式 |
| [examples](examples) | 16份有效示例；不是实际HTTP记录 |
| [pricing-vectors.json](pricing-vectors.json) | 23个独立预期算例，含分摊、互斥、门槛、零元和long边界 |
| [validation](validation) | 独立设计校验器、示例映射、源码基线与运行说明 |

首期决策：订单满减/折扣至多一个，加一张用户指定券；在选券约束下择优，不自动丢券。活动先算、券后算，逐阶段按最大余数分摊。零元如实展示但不成单；0分券不核销。提单只用服务端缓存快照，变更后确认终止原意图再重结算。支付确认与关闭释放的权益账本同交易库事务。

下一实施起点为T4服务端缓存结算V2，再按依赖接RPC、V2缓存/生命周期和页面。秒杀/拼团/砍价仍需各自业务状态设计；没有新微服务或依赖；V9仅交付文件、未在目标环境执行。未merge/deploy，保留Draft。
