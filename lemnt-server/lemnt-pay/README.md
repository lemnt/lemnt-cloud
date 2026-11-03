# 支付服务

支付、原路退款、钱包与充值独立存储；order拥有订单/售后状态。唯一初始化SQL为sql/lemnt_pay.sql，只用于空库。端口默认9170，通过PAY_PORT覆盖。配置由环境提供，不存储凭据。

从lemnt-server/pom.xml使用-pl lemnt-pay/lemnt-pay-start -am构建。新事件必须经Outbox投递，旧AFTER_SALE任务须在切流前审核迁往order；不可直接启用双写消费者。
