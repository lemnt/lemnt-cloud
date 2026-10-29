# member 结构与规范整理

输入：分析并调整 member 项目结构、代码规范和域划分，参考 system；删除短信和微信逻辑；修改后不提交。

- [分析与开发设计](03-development-design.md)
- [实现与验证](05-development.md)

保留既有 app/web、认证位于 auth、公共 TokenService/LOGIN_TOKEN_KEY、网关无 Dubbo、无会员全设备会话索引的决定。

- [商城复刻基线](22-mall-replication-baseline.md)
- [配送基础批次设计](23-delivery-foundation-design.md)
- [配送基础批次交付](24-delivery-foundation-delivery.md)
- [配送管理接口](../../docs/trade-delivery-api.md)

- [配送结算贯通设计](25-delivery-checkout-design.md)
- [配送结算贯通交付](26-delivery-checkout-delivery.md)
- [配送结算接口](../../docs/delivery-checkout-api.md)

- [完整迁移设计与清单](27-full-migration-design.md)
- [优惠券快照迁移设计](28-full-coupon-design.md)
- [优惠券当前接口](../../docs/coupon-migration-api.md)

- [订单日志迁移设计](29-order-log-design.md)
- [订单日志及本人操作接口](../../docs/order-operation-log-api.md)

- [优惠券与订单日志实现及验证](30-coupon-order-delivery.md)

- [售后标准表与日志设计](31-aftersale-log-design.md)
- [售后迁移交付与验证](32-aftersale-log-delivery.md)
- [售后当前接口](../../docs/after-sale-log-api.md)

- [原订单商品快照设计](33-order-product-snapshot-design.md)
- [商品快照交付与验证](34-order-product-snapshot-delivery.md)
- [订单与售后快照接口](../../docs/order-product-snapshot-api.md)

- [商品规格字典关联设计](35-product-spec-identity-design.md)
- [规格字典关联交付](36-product-spec-identity-delivery.md)
- [规格关联接口](../../docs/product-spec-identity-api.md)

- [完整规格快照列表设计](37-property-snapshot-list-design.md)
- [规格列表贯通交付](38-property-snapshot-list-delivery.md)

- [商品规格列表合同设计](39-product-property-contract-design.md)
- [商品规格合同收敛交付](40-product-property-contract-delivery.md)

- [SKU规格存储合并设计](41-sku-property-storage-design.md)
- [SKU JSON存储合并交付](42-sku-property-storage-delivery.md)

- [商品收藏与浏览足迹设计](43-product-interaction-design.md)
- [商品收藏与浏览足迹交付](44-product-interaction-delivery.md)
- [商品收藏与足迹接口](../../docs/product-interaction-api.md)

- [文章相关迁移范围调整](45-article-scope-adjustment.md)
