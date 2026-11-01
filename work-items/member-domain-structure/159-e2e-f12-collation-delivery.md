# 端到端验收与 F12 收口交付

2026-11-01 第159批。完成浏览器后台端到端验收、Nacos 路由核对、F12 对照收口、兼容构造器清理和生产 SQL 发布脚本。

## 端到端验收

- 通过网关验证码登录后台管理员，未跳过验证码校验。
- 浏览器实测：
  - 自提核销台菜单与页面可打开，查询核销码 `38846600`，确认核销后订单 `TR3759C6A7A75E4455974A37CF` 转 `COMPLETED`。
  - 订单履约页展示“改价/改址”操作，改价弹窗读取当前应付 `8.00` 并可提交。
- Nacos `lemnt-gateway.yaml` 核对：`/mall/**`、`/trade/**` 保留服务前缀转发，`/auth/**`、`/system/**` StripPrefix=1；登录白名单仅 `auth/sessions/login` 等，新增接口均走既有前缀。
- 经网关实测通过：`/mall/app/checkout/quote`、`/mall/app/checkout`、`/trade/app/orders*`、`/trade/app/delivery/pick-up-stores`、`/trade/admin/orders/{no}/price-adjustment`、`/trade/admin/orders/{no}/address`、`/trade/admin/after-sales/*`、`/system/menus/routers`。

## F12 对照

- 当前代码规模（自动统计）：mall 189 个接口、trade 119 个、member 62 个；mall 35 张、trade 27 张、member 18 张业务表；8 个定时任务；React 56 条路由；小程序 38 个页面。
- 本批新增/修改对照：
  - 接口：订单调价、订单改址、门店距离查询、待自提列表/数量。
  - 表字段：`trade_order.give_coupon_template_counts`、`mall_coupon.source_order_no`。
  - 任务：部分退款佣金扣回在退款事务内执行，不新增调度任务。
  - 页面：店员核销台、履约页改价/改址弹窗、小程序门店距离排序、订单中心待自提页签。
- 63 单元台账的 2026-10-31 估算（42.3%）不再作为当前总进度引用；第128–159批已补齐其中促销运营、支付/钱包、分销、自提、客服/装修、活动聚合等主要0分单元，但本批未重新做等权复核，不给出新百分比。

## 兼容残留清理

删除已无调用的兼容构造器：`SettlementCreateCmd` 5/3参、`SettlementQuoteDTO` 9/8参、`SettlementRes` 9参、`OrderDeliveryDTO` 7参、`SettlementDelivery` 7参、`AppPickUpStoreDTO/Res` 10参、`MemberOrderStatusCountDTO` 7参、`AppOrderStatusCountRes` 7参、`OrderStatusCount` 3参。仍被测试或历史快照使用的构造器保留。

## 生产 SQL 发布

新增 `lemnt-server/sql/release/apply-business-sql.sh` 与 README：按版本号顺序执行 member/mall/trade 建表脚本和系统菜单脚本，默认试运行，`--apply` 才写库；系统基础表不重建，已有环境补列继续使用各模块 `sql/upgrade/` 人工脚本。

## 验证

- mall 190、trade 272、网关 35、React 315、小程序 176 项测试通过。
- 未执行：小程序真机、iOS/Android/H5、真实支付宝/微信支付退款转账、真实物流轨迹。
- 本批未 commit/push。
