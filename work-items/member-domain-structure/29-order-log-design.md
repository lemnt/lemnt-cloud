# 完整迁移F02：订单状态日志

2026-10-28 v1。承接27-full-migration-design.md；按code-software-engineer development-design及api-documentation示例裁剪。来源为本地芋道TradeOrderLogDO、TradeOrderOperateTypeEnum和TradeOrderLogService，sourceSHA纳入总清单。

目标是订单创建、支付、发货、收货、取消、全额售后退款有同事务日志，重放不重复；不是只新增空表或通用CRUD。新增trade_order_log及order域逐表DO/BaseMapper/XML resultMap/Gateway。应用服务编排订单状态更新和日志，Gateway不跨表。日志关联order_id，不用业务订单号伪装数值ID；before_status/after_status按芋道0待支付、10待发货、20待收货、30完成、40取消表达。当前内部RESERVING与PENDING_PAYMENT均映射0，内部预占完成不额外写同状态日志。

actor明确记录SYSTEM(0,0)、MEMBER(会员ID,1)、ADMIN(后台ID,2)。由HTTP Adapter从已经验证的上下文构造操作者，不接受客户端用户类型；后台/渠道回调及定时任务按实际发起方区分。会员收货和取消在订单行锁内核对归属。发货保留真实后台操作者。source没有实现的后台手工取消不能假写成超时取消。操作类型沿用source1/10/20/30/31/40/41/43，后续调价、改址、评价分别补2/11/33/34。

前端订单详情展示日志；后台查询权限复用trade:fulfillment:query，接口GET /trade/admin/orders/{orderNo}/logs返回独立Res。对照源项目，操作日志保留后台查询，不新增App内部日志入口；App补POST /trade/app/orders/{orderNo}/cancel及/receive，仅本人可操作。字段id/userId/userType/orderId/beforeStatus/afterStatus/operateType/content/createTime，null初态允许缺失，createTime数据库同一时区。统一错误：非法参数422、业务/越权按现有服务异常规则拒绝，不新增异常包装。

验证：真实H2持久化事务链创建→支付→发货→收货及关单/退款日志顺序；重复通知/重复收货/重复关单无重复；人为日志落库失败时状态回滚；本人越权取消/收货拒绝。真实MySQL、Dubbo及浏览器端到端分别标为未运行。新库初始化脚本、旧库增量建表不回填虚构历史日志；不commit、执行真实DDL或部署。

售后日志必须在售后拥有源项目数值ID后关联after_sale_id；不能用refundNo伪装该字段。本批先落真实订单日志，售后ID/全字段/日志继续按F02逐项迁移，不能据此标记F02全部完成。
