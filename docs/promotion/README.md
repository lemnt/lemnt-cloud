# 促销引擎设计与实施

promotion-v1 · 2026-10-25（Asia/Shanghai）· 基础20ec7bd。

**T1纯Java计价内核已实现；业务链路尚未接入。** 实施与验证见[implementation.md](implementation.md)。T2..T7及11个拟定HTTP接口仍待实现；现有优惠券、结算、支付和退款行为保持兼容。

| 文件 | 内容 |
| --- | --- |
| [design.md](design.md) | 首期规则、组合/互斥、整数分计算与分摊、缓存提单、活动占用/补偿、数据模型和T1..T7任务 |
| [api.md](api.md) | 11个拟新增管理/小程序入口、完整字段、权限、错误与构造示例 |
| [contracts](contracts) | 15份JSON Schema，包含自定义int64边界格式 |
| [examples](examples) | 16份有效示例；不是实际HTTP记录 |
| [pricing-vectors.json](pricing-vectors.json) | 23个独立预期算例，含分摊、互斥、门槛、零元和long边界 |
| [validation](validation) | 独立设计校验器、示例映射、源码基线与运行说明 |

首期决策：订单满减/折扣至多一个，加一张用户指定券；在选券约束下择优，不自动丢券。活动先算、券后算，逐阶段按最大余数分摊。零元如实展示但不成单；0分券不核销。提单只用服务端缓存快照，变更后确认终止原意图再重结算。支付确认与关闭释放的权益账本同交易库事务。

下一实施起点为T2活动版本/次数持久化，再按依赖接RPC、V2缓存/生命周期和页面。秒杀/拼团/砍价仍需各自业务状态设计；本次不新增营销微服务、运行时依赖或可执行SQL。未merge/deploy，保留Draft。
