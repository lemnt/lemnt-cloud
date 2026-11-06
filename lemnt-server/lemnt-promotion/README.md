# 营销服务

活动、优惠券、购物车、DIY和客服独立存储。商品仅通过product-client读取，订单生命周期由order事件驱动。唯一初始化文件sql/lemnt_promotion.sql，默认端口9150。

从lemnt-server/pom.xml使用-pl lemnt-promotion/lemnt-promotion-start -am构建。新模块不得连接原mall库；真实环境切流前须排空或核对原任务。

P5-00配置源位于`docs/config/lemnt-promotion.yaml`及共享`commerce-mq.yaml`。消费者由`COMMERCE_MQ_ENABLED=true`
显式开启，默认关闭；本服务只有Inbox，不配置Outbox
Relay。先完成旧写入方停用和原任务审核，再按[配置与切流准备](../../docs/module-split-cutover.md)启用；本批未发布配置或切流。
