# 商品服务

包含商品目录、规格、互动评价与库存。端口默认9140，通过PRODUCT_PORT覆盖。数据源、Dubbo、MQ参数由lemnt-product.yaml提供；不得连接旧mall库来掩盖表归属。

唯一初始化文件：sql/lemnt_product.sql。仅适用于空库，现有库需按工作项切流说明迁移并核对。公共HTTP仍使用/mall前缀，完成全部迁移前不替换现有网关。

构建：从lemnt-server/pom.xml运行-pl lemnt-product/lemnt-product-start -am clean package。真实MQ、Nacos、Dubbo和HTTP验收单独记录。

P5-00配置源位于`docs/config/lemnt-product.yaml`及共享`commerce-mq.yaml`。消费者由`COMMERCE_MQ_ENABLED=true`显式开启，默认关闭；Outbox Relay还需`COMMERCE_OUTBOX_ENABLED=true`。先完成旧写入方停用和原任务审核，再按[配置与切流准备](../../docs/module-split-cutover.md)启用；本批未发布配置或切流。
