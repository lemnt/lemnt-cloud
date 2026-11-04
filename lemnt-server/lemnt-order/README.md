# 订单服务

结算、订单明细、售后、履约、物流、自提、分佣及原优惠/活动扩展独立存储。支付、商品、营销只通过Client与事件衔接。端口默认9160；唯一初始化SQL为sql/lemnt_order.sql，只用于空库。

从lemnt-server/pom.xml以-pl lemnt-order/lemnt-order-start -am构建。真实数据切流需先核对快照、未决任务与资源凭证，不能直接修改现有库。

恢复任务由 `lemnt.order.recovery.enabled=true` 显式启用；每轮有界扫描支付后处理、退款执行及状态10的提现，重试复用原编号。应在旧写入方停止、未决任务与资源凭证审核完成后启用。旧补偿Topic适配由 `lemnt.order.legacy-rollback.enabled` 控制，默认关闭；正式发送仍使用约定8类业务事件。

P4-00～07已完成代码与自动化验证。五服务不跳过测试的clean package通过，共716项测试，其中order含start共302项；失败、错误、跳过均0。最终夹具清理后order完整package再次通过。组合验证使用独立H2库及生产业务组件，RPC/MQ传输、会员与外部渠道边界为测试替身；真实MySQL、Nacos、Dubbo/MQ网络和网关HTTP仍未验收，P5-01网关内置映射已实现，在线切流与旧模块清理尚未执行。详情见工作项`work-items/module-split-migration/code-summary.md`及`evidence/p4-order-combination-tests.json`。

P5-00配置源位于`docs/config/lemnt-order.yaml`及共享`commerce-mq.yaml`。消费者由`COMMERCE_MQ_ENABLED=true`显式开启，默认关闭；Outbox Relay还需`COMMERCE_OUTBOX_ENABLED=true`。先完成旧写入方停用和原任务审核，再按[配置与切流准备](../../docs/module-split-cutover.md)启用；本批未发布配置或切流。
