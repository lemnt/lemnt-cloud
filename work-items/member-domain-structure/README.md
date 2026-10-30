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

- [商品评价链路设计](46-product-comment-design.md)
- [商品评价链路交付](47-product-comment-delivery.md)
- [商品评价当前接口](../../docs/product-comment-api.md)

- [订单项评价联动设计](48-order-item-comment-design.md)
- [订单项评价联动交付](49-order-item-comment-delivery.md)
- [订单项评价接口](../../docs/order-item-comment-api.md)

- [后台评价筛选设计](50-comment-filter-design.md)
- [后台评价筛选交付](51-comment-filter-delivery.md)

- [商品评价源差异复核及清单纠正](52-comment-source-audit.md)

- [后台自评设计](53-comment-self-design.md)
- [后台自评交付](54-comment-self-delivery.md)

- [收货时间迁移设计](55-order-receive-time-design.md)
- [收货时间迁移交付](56-order-receive-time-delivery.md)

- [系统评价调度设计](57-system-comment-design.md)
- [系统评价调度交付](58-system-comment-delivery.md)

- [同页多商品评价设计](59-order-comment-page-design.md)
- [同页多商品评价交付](60-order-comment-page-delivery.md)

- [评价图片上传设计](61-comment-upload-design.md)
- [评价图片上传交付](62-comment-upload-delivery.md)

- [评价图片交互设计](63-comment-image-interaction-design.md)
- [评价图片交互交付](64-comment-image-interaction-delivery.md)

- [会员后台备注设计](65-member-mark-design.md)
- [会员后台备注交付](66-member-mark-delivery.md)

- [会员姓名地区设计](67-member-identity-design.md)
- [会员姓名地区交付](68-member-identity-delivery.md)

- [会员地区选择设计](69-member-area-design.md)
- [会员地区选择交付](70-member-area-delivery.md)

- [会员登录审计时间设计](71-member-login-audit-design.md)
- [会员登录审计时间交付](72-member-login-audit-delivery.md)

- [会员登录连接IP设计](73-member-login-ip-design.md)
- [会员登录连接IP交付](74-member-login-ip-delivery.md)

- [后台签到昵称筛选设计](75-sign-record-nickname-design.md)
- [后台签到昵称筛选交付](76-sign-record-nickname-delivery.md)

- [后台积分流水筛选设计](77-point-record-filter-design.md)
- [后台积分流水筛选交付](78-point-record-filter-delivery.md)

- [积分业务元数据快照设计](79-point-record-metadata-design.md)
- [积分业务元数据快照交付](80-point-record-metadata-delivery.md)

- [积分数值类型筛选设计](81-point-numeric-filter-design.md)
- [积分数值类型筛选交付](82-point-numeric-filter-delivery.md)
