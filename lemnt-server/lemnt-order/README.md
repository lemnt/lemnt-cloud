# 订单服务

结算、订单明细、售后、履约、物流、自提、分佣及原优惠/活动扩展独立存储。支付、商品、营销只通过Client与事件衔接。端口默认9160；唯一初始化SQL为sql/lemnt_order.sql，只用于空库。

从lemnt-server/pom.xml以-pl lemnt-order/lemnt-order-start -am构建。真实数据切流需先核对快照、未决任务与资源凭证，不能直接修改现有库。
