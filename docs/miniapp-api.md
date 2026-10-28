# 小程序商城与交易接口

> 2026-10-28 配送贯通批次：商品配送字段、地址必填结算、订单运费快照与发货冻结地址见 [配送结算接口变更](delivery-checkout-api.md)。本批已将运费接入下单金额；原文中“尚未接入”的说明仅代表上一批次状态。

购物接口由 mall 提供，支付接口由 trade 提供。auth 签发会员令牌，gateway 校验并透传用户身份；不接收客户端 memberId 作为身份，不在业务服务重复认证 token。接口使用统一 R，金额使用精确字符串。

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | /mall/app/cart | 本人购物车 |
| PUT/DELETE | /mall/app/cart/{productId} | 保存/删除本人购物车条目 |
| POST | /mall/app/cart/{productId}/add | 设置购物车最终数量 |
| GET | /mall/app/coupons | 本人优惠券 |
| GET | /mall/app/coupon-center | 可领取券分页 |
| POST | /mall/app/coupon-center/{templateId}/claim | 领取公开优惠券 |
| GET | /mall/app/coupon-wallet | 本人券包分页 |
| POST | /mall/app/checkout/quote | 创建仅缓存 Redis 的结算单 |
| POST | /mall/app/checkout | 按 settlementId/requestNo 同步提交，完成核券锁券及预占库存后返回 |
| POST | /mall/app/checkout/abandon | 终止未接受的请求或返回原订单 |
| GET | /mall/app/checkout/requests/{requestNo} | 找回本人提单结果 |
| GET | /mall/app/orders/{orderNo} | 本人结算回执 |
| GET | /trade/app/orders/{orderNo}/payment | 本地支付单状态，不冒充真实渠道查询 |

商品、类目接口继续使用 `/mall/app/products`、`/mall/app/products/{id}`、`/mall/app/categories`。购物车、结算与券接口需要会员身份；公开商品目录沿用网关既有边界。

完整结算协议见 [Redis 结算单](cached-settlement-api.md)。服务事务、缓存和数据切换见 [服务边界](mall-trade-boundary.md)。没有新增真实预支付接口，也没有自动修改小程序上线开关或实际环境配置。
