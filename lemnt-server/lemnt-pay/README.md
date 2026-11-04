# 支付服务

支付、原路退款、钱包与充值独立存储；order拥有订单/售后状态。唯一初始化SQL为sql/lemnt_pay.sql，只用于空库。端口默认9170，通过PAY_PORT覆盖。配置由环境提供，不存储凭据。

从lemnt-server/pom.xml使用-pl lemnt-pay/lemnt-pay-start -am构建。新事件必须经Outbox投递，旧AFTER_SALE任务须在切流前审核迁往order；不可直接启用双写消费者。

P5-00配置源位于`docs/config/lemnt-pay.yaml`及共享`commerce-mq.yaml`。消费者由`COMMERCE_MQ_ENABLED=true`显式开启，默认关闭；Outbox
Relay还需`COMMERCE_OUTBOX_ENABLED=true`
。先完成旧写入方停用和原任务审核，再按[配置与切流准备](../../docs/module-split-cutover.md)启用；本批未发布配置或切流。
