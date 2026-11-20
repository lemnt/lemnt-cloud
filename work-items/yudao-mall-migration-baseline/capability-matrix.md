# 全量源码入口能力映射（首轮盘点）

> 2026-11-06。按本地源码Controller能力族完整枚举；不是所有业务已经逐方法验收。机械清单见[evidence/capability-inventory.json](evidence/capability-inventory.json)。
> 入口存在不代表业务等价。状态分开记录：入口盘点、语义设计、实施、自动化、本地实际验收。尚未核实不得标记已迁移完成。

| ID | 源上下文/能力族 | 源入口数 | 目标承接候选 | 语义核对/设计状态 | 实施及真实验收 |
|---|---|---:|---|---|---|
| C001 | member/address | 2 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C002 | member/auth | 1 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C003 | member/config | 1 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C004 | member/group | 1 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C005 | member/level | 5 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C006 | member/point | 2 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C007 | member/signin | 4 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C008 | member/social | 1 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C009 | member/tag | 1 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C010 | member/user | 2 | lemnt-member / auth适配 | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C011 | pay/app | 1 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C012 | pay/channel | 2 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C013 | pay/demo | 2 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C014 | pay/notify | 1 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C015 | pay/order | 2 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C016 | pay/refund | 1 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C017 | pay/transfer | 2 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C018 | pay/wallet | 8 | lemnt-pay | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C019 | product/brand | 1 | lemnt-product | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C020 | product/category | 2 | lemnt-product | 代表链已核实差异；见source-analysis F04/F05/F08 | 未在本工作项实施/未本地验收 |
| C021 | product/comment | 2 | lemnt-product | 代表链已核实差异；见source-analysis F04/F05/F08 | 未在本工作项实施/未本地验收 |
| C022 | product/favorite | 2 | lemnt-product | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C023 | product/history | 2 | lemnt-product | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C024 | product/property | 2 | lemnt-product | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C025 | product/spu | 2 | lemnt-product | 代表链已核实差异；见source-analysis F04/F05/F08 | 未在本工作项实施/未本地验收 |
| C026 | promotion/activity | 1 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C027 | promotion/article | 4 | lemnt-promotion | 未发现明确同类入口；迁移缺口候选，尚不能以名称断言完全缺失 | 未在本工作项实施/未本地验收 |
| C028 | promotion/banner | 2 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C029 | promotion/bargain | 6 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C030 | promotion/combination | 4 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C031 | promotion/coupon | 4 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C032 | promotion/discount | 1 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C033 | promotion/diy | 4 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C034 | promotion/kefu | 3 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C035 | promotion/point | 2 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C036 | promotion/reward | 2 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C037 | promotion/seckill | 4 | lemnt-promotion | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C038 | statistics/member | 1 | 既有上下文只读聚合（归属待逐项定） | 源入口已确认；当前仅见订单简要统计，逐指标核对后补迁 | 未在本工作项实施/未本地验收 |
| C039 | statistics/pay | 1 | 既有上下文只读聚合（归属待逐项定） | 源入口已确认；当前仅见订单简要统计，逐指标核对后补迁 | 未在本工作项实施/未本地验收 |
| C040 | statistics/product | 1 | 既有上下文只读聚合（归属待逐项定） | 源入口已确认；当前仅见订单简要统计，逐指标核对后补迁 | 未在本工作项实施/未本地验收 |
| C041 | statistics/trade | 1 | 既有上下文只读聚合（归属待逐项定） | 源入口已确认；当前仅见订单简要统计，逐指标核对后补迁 | 未在本工作项实施/未本地验收 |
| C042 | trade/aftersale | 3 | lemnt-order | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C043 | trade/brokerage | 6 | lemnt-order | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C044 | trade/cart | 1 | lemnt-order | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C045 | trade/config | 2 | lemnt-order | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C046 | trade/delivery | 5 | lemnt-order | 待逐方法核对；属于全量迁移范围，不移出为二开 | 未在本工作项实施/未本地验收 |
| C047 | trade/order | 2 | lemnt-order | 下单/支付/关单/评价代表链已核对；退款等仍需逐项补齐 | 未在本工作项实施/未本地验收 |

## 可追溯的源入口

下列每个入口带源码文件、类级路径行号和能力族。方法级参数、响应及规则需与Service/Mapper联合核对，不能直接由注解生成“业务已完成”结论。

- `product/brand admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/brand/ProductBrandController.java:L26`；类级路径 `/product/brand`。
- `product/category admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/category/ProductCategoryController.java:L26`；类级路径 `/product/category`。
- `product/comment admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/comment/ProductCommentController.java:L24`；类级路径 `/product/comment`。
- `product/favorite admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/favorite/ProductFavoriteController.java:L30`；类级路径 `/product/favorite`。
- `product/history admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/history/ProductBrowseHistoryController.java:L31`；类级路径 `/product/browse-history`。
- `product/property admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/property/ProductPropertyController.java:L27`；类级路径 `/product/property`。
- `product/property admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/property/ProductPropertyValueController.java:L28`；类级路径 `/product/property/value`。
- `product/spu admin`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/spu/ProductSpuController.java:L37`；类级路径 `/product/spu`。
- `product/category app`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/category/AppCategoryController.java:L28`；类级路径 `/product/category`。
- `product/comment app`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/comment/AppProductCommentController.java:L26`；类级路径 `/product/comment`。
- `product/favorite app`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/favorite/AppFavoriteController.java:L28`；类级路径 `/product/favorite`。
- `product/history app`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/history/AppProductBrowseHistoryController.java:L32`；类级路径 `/product/browse-history`。
- `product/spu app`：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/spu/AppProductSpuController.java:L40`；类级路径 `/product/spu`。
- `promotion/article admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/article/ArticleCategoryController.java:L26`；类级路径 `/promotion/article-category`。
- `promotion/article admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/article/ArticleController.java:L26`；类级路径 `/promotion/article`。
- `promotion/banner admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/banner/BannerController.java:L26`；类级路径 `/promotion/banner`。
- `promotion/bargain admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/bargain/BargainActivityController.java:L33`；类级路径 `/promotion/bargain-activity`。
- `promotion/bargain admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/bargain/BargainHelpController.java:L30`；类级路径 `/promotion/bargain-help`。
- `promotion/bargain admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/bargain/BargainRecordController.java:L34`；类级路径 `/promotion/bargain-record`。
- `promotion/combination admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/combination/CombinationActivityController.java:L38`；类级路径 `/promotion/combination-activity`。
- `promotion/combination admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/combination/CombinationRecordController.java:L34`；类级路径 `/promotion/combination-record`。
- `promotion/coupon admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/coupon/CouponController.java:L33`；类级路径 `/promotion/coupon`。
- `promotion/coupon admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/coupon/CouponTemplateController.java:L25`；类级路径 `/promotion/coupon-template`。
- `promotion/discount admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/discount/DiscountActivityController.java:L30`；类级路径 `/promotion/discount-activity`。
- `promotion/diy admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/diy/DiyPageController.java:L25`；类级路径 `/promotion/diy-page`。
- `promotion/diy admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/diy/DiyTemplateController.java:L26`；类级路径 `/promotion/diy-template`。
- `promotion/kefu admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/kefu/KeFuConversationController.java:L29`；类级路径 `/promotion/kefu-conversation`。
- `promotion/kefu admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/kefu/KeFuMessageController.java:L33`；类级路径 `/promotion/kefu-message`。
- `promotion/point admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/point/PointActivityController.java:L36`；类级路径 `/promotion/point-activity`。
- `promotion/reward admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/reward/RewardActivityController.java:L26`；类级路径 `/promotion/reward-activity`。
- `promotion/seckill admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/seckill/SeckillActivityController.java:L32`；类级路径 `/promotion/seckill-activity`。
- `promotion/seckill admin`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/seckill/SeckillConfigController.java:L25`；类级路径 `/promotion/seckill-config`。
- `promotion/activity app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/activity/AppActivityController.java:L30`；类级路径 `/promotion/activity`。
- `promotion/article app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/article/AppArticleCategoryController.java:L23`；类级路径 `/promotion/article-category`。
- `promotion/article app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/article/AppArticleController.java:L29`；类级路径 `/promotion/article`。
- `promotion/banner app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/banner/AppBannerController.java:L21`；类级路径 `/promotion/banner`。
- `promotion/bargain app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/bargain/AppBargainActivityController.java:L39`；类级路径 `/promotion/bargain-activity`。
- `promotion/bargain app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/bargain/AppBargainHelpController.java:L29`；类级路径 `/promotion/bargain-help`。
- `promotion/bargain app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/bargain/AppBargainRecordController.java:L44`；类级路径 `/promotion/bargain-record`。
- `promotion/combination app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/combination/AppCombinationActivityController.java:L36`；类级路径 `/promotion/combination-activity`。
- `promotion/combination app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/combination/AppCombinationRecordController.java:L37`；类级路径 `/promotion/combination-record`。
- `promotion/coupon app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/coupon/AppCouponController.java:L31`；类级路径 `/promotion/coupon`。
- `promotion/coupon app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/coupon/AppCouponTemplateController.java:L37`；类级路径 `/promotion/coupon-template`。
- `promotion/diy app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/diy/AppDiyPageController.java:L24`；类级路径 `/promotion/diy-page`。
- `promotion/diy app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/diy/AppDiyTemplateController.java:L29`；类级路径 `/promotion/diy-template`。
- `promotion/kefu app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/kefu/AppKeFuMessageController.java:L32`；类级路径 `/promotion/kefu-message`。
- `promotion/point app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/point/AppPointActivityController.java:L39`；类级路径 `/promotion/point-activity`。
- `promotion/reward app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/reward/AppRewardActivityController.java:L24`；类级路径 `/promotion/reward-activity`。
- `promotion/seckill app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/seckill/AppSeckillActivityController.java:L50`；类级路径 `/promotion/seckill-activity`。
- `promotion/seckill app`：`yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/app/seckill/AppSeckillConfigController.java:L24`；类级路径 `/promotion/seckill-config`。
- `trade/aftersale admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/aftersale/AfterSaleController.java:L41`；类级路径 `/trade/after-sale`。
- `trade/brokerage admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/brokerage/BrokerageRecordController.java:L33`；类级路径 `/trade/brokerage-record`。
- `trade/brokerage admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/brokerage/BrokerageUserController.java:L37`；类级路径 `/trade/brokerage-user`。
- `trade/brokerage admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/brokerage/BrokerageWithdrawController.java:L34`；类级路径 `/trade/brokerage-withdraw`。
- `trade/config admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/config/TradeConfigController.java:L23`；类级路径 `/trade/config`。
- `trade/delivery admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/delivery/DeliveryExpressController.java:L30`；类级路径 `/trade/delivery/express`。
- `trade/delivery admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/delivery/DeliveryExpressTemplateController.java:L25`；类级路径 `/trade/delivery/express-template`。
- `trade/delivery admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/delivery/DeliveryPickUpStoreController.java:L31`；类级路径 `/trade/delivery/pick-up-store`。
- `trade/order admin`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/order/TradeOrderController.java:L37`；类级路径 `/trade/order`。
- `trade/aftersale app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/aftersale/AppAfterSaleController.java:L27`；类级路径 `/trade/after-sale`。
- `trade/aftersale app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/aftersale/AppAfterSaleLogController.java:L30`；类级路径 `/trade/after-sale-log`。
- `trade/brokerage app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/brokerage/AppBrokerageRecordController.java:L31`；类级路径 `/trade/brokerage-record`。
- `trade/brokerage app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/brokerage/AppBrokerageUserController.java:L42`；类级路径 `/trade/brokerage-user`。
- `trade/brokerage app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/brokerage/AppBrokerageWithdrawController.java:L35`；类级路径 `/trade/brokerage-withdraw`。
- `trade/cart app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/cart/AppCartController.java:L23`；类级路径 `/trade/cart`。
- `trade/config app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/config/AppTradeConfigController.java:L26`；类级路径 `/trade/config`。
- `trade/delivery app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/delivery/AppDeliverExpressController.java:L25`；类级路径 `/trade/delivery/express`。
- `trade/delivery app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/delivery/AppDeliverPickUpStoreController.java:L27`；类级路径 `/trade/delivery/pick-up-store`。
- `trade/order app`：`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/order/AppTradeOrderController.java:L41`；类级路径 `/trade/order`。
- `statistics/member admin`：`yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/controller/admin/member/MemberStatisticsController.java:L30`；类级路径 `/statistics/member`。
- `statistics/pay admin`：`yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/controller/admin/pay/PayStatisticsController.java:L21`；类级路径 `/statistics/pay`。
- `statistics/product admin`：`yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/controller/admin/product/ProductStatisticsController.java:L38`；类级路径 `/statistics/product`。
- `statistics/trade admin`：`yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/controller/admin/trade/TradeStatisticsController.java:L38`；类级路径 `/statistics/trade`。
- `member/address admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/address/AddressController.java:L25`；类级路径 `/member/address`。
- `member/config admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/config/MemberConfigController.java:L22`；类级路径 `/member/config`。
- `member/group admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/group/MemberGroupController.java:L25`；类级路径 `/member/group`。
- `member/level admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/level/MemberExperienceRecordController.java:L27`；类级路径 `/member/experience-record`。
- `member/level admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/level/MemberLevelController.java:L23`；类级路径 `/member/level`。
- `member/level admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/level/MemberLevelRecordController.java:L27`；类级路径 `/member/level-record`。
- `member/point admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/point/MemberPointRecordController.java:L30`；类级路径 `/member/point/record`。
- `member/signin admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/signin/MemberSignInConfigController.java:L26`；类级路径 `/member/sign-in/config`。
- `member/signin admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/signin/MemberSignInRecordController.java:L30`；类级路径 `/member/sign-in/record`。
- `member/tag admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/tag/MemberTagController.java:L28`；类级路径 `/member/tag`。
- `member/user admin`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/admin/user/MemberUserController.java:L39`；类级路径 `/member/user`。
- `member/address app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/address/AppAddressController.java:L25`；类级路径 `/member/address`。
- `member/auth app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/auth/AppAuthController.java:L31`；类级路径 `/member/auth`。
- `member/level app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/level/AppMemberExperienceRecordController.java:L25`；类级路径 `/member/experience-record`。
- `member/level app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/level/AppMemberLevelController.java:L23`；类级路径 `/member/level`。
- `member/point app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/point/AppMemberPointRecordController.java:L27`；类级路径 `/member/point/record`。
- `member/signin app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/signin/AppMemberSignInConfigController.java:L24`；类级路径 `/member/sign-in/config`。
- `member/signin app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/signin/AppMemberSignInRecordController.java:L26`；类级路径 `/member/sign-in/record`。
- `member/social app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/social/AppSocialUserController.java:L27`；类级路径 `/member/social-user`。
- `member/user app`：`yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller/app/user/AppMemberUserController.java:L25`；类级路径 `/member/user`。
- `pay/app admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/app/PayAppController.java:L31`；类级路径 `/pay/app`。
- `pay/channel admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/channel/PayChannelController.java:L27`；类级路径 `/pay/channel`。
- `pay/demo admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/demo/PayDemoOrderController.java:L29`；类级路径 `/pay/demo-order`。
- `pay/demo admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/demo/PayDemoWithdrawController.java:L27`；类级路径 `/pay/demo-withdraw`。
- `pay/notify admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/notify/PayNotifyController.java:L45`；类级路径 `/pay/notify`。
- `pay/order admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/order/PayOrderController.java:L48`；类级路径 `/pay/order`。
- `pay/refund admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/refund/PayRefundController.java:L37`；类级路径 `/pay/refund`。
- `pay/transfer admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/transfer/PayTransferController.java:L36`；类级路径 `/pay/transfer`。
- `pay/wallet admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/wallet/PayWalletController.java:L30`；类级路径 `/pay/wallet`。
- `pay/wallet admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/wallet/PayWalletRechargeController.java:L23`；类级路径 `/pay/wallet-recharge`。
- `pay/wallet admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/wallet/PayWalletRechargePackageController.java:L27`；类级路径 `/pay/wallet-recharge-package`。
- `pay/wallet admin`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/wallet/PayWalletTransactionController.java:L26`；类级路径 `/pay/wallet-transaction`。
- `pay/channel app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/channel/AppPayChannelController.java:L24`；类级路径 `/pay/channel`。
- `pay/order app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/order/AppPayOrderController.java:L39`；类级路径 `/pay/order`。
- `pay/transfer app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/transfer/AppPayTransferController.java:L21`；类级路径 `/pay/transfer`。
- `pay/wallet app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/wallet/AppPayWalletController.java:L24`；类级路径 `/pay/wallet`。
- `pay/wallet app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/wallet/AppPayWalletRechargeController.java:L39`；类级路径 `/pay/wallet-recharge`。
- `pay/wallet app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/wallet/AppPayWalletRechargePackageController.java:L25`；类级路径 `/pay/wallet-recharge-package`。
- `pay/wallet app`：`yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/app/wallet/AppPayWalletTransactionController.java:L34`；类级路径 `/pay/wallet-transaction`。

## 进度口径

- 控制器入口盘点：已执行本次机械扫描，所有记录带SHA-256。
- 全业务语义覆盖：尚未完成；代表链证据来自已确认源码分析。
- 测试/本地运行：本次设计阶段未执行。
- 判断“全量迁移完成”时，此表每个能力族及其方法必须落到可核验结果；不能以原313端点契约代替本清单。
