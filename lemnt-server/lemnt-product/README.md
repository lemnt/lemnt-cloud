# 商品服务

包含商品目录、规格、互动评价与库存。端口默认9140，通过PRODUCT_PORT覆盖。数据源、Dubbo、MQ参数由lemnt-product.yaml提供；不得连接旧mall库来掩盖表归属。

唯一初始化文件：sql/lemnt_product.sql。仅适用于空库，现有库需按工作项切流说明迁移并核对。公共HTTP仍使用/mall前缀，完成全部迁移前不替换现有网关。

构建：从lemnt-server/pom.xml运行-pl lemnt-product/lemnt-product-start -am clean package。真实MQ、Nacos、Dubbo和HTTP验收单独记录。
