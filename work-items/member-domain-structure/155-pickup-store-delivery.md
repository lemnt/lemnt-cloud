# 自提门店、店员与核销交付

2026-11-01 第155批。补齐自提门店管理、核销店员绑定、自提订单设置、核销码核销、小程序门店列表与订单自提信息。买家在结算时选择自提门店的完整链路仍未接入。

## 数据

- 新增 `trade_delivery_pick_up_store`（`V16__pickup.sql`）：名称、简介、电话、地区、地址、logo、营业时间、经纬度、核销店员、状态。
- `trade_order` 增加 `pick_up_store_id`、`pick_up_verify_code`，核销码全局唯一。
- 升级脚本：`upgrade/pickup_store.sql`。

## 业务

- 后台 `/trade/admin/delivery/pick-up-stores`：分页、详情、新增、修改、删除、绑定核销店员；营业时间必须递增，店员最多 20 人且不重复。
- 后台 `/trade/admin/orders/pick-up`：把已支付、无处理中售后的订单设置为自提，生成 8 位核销码并写操作日志；可按码预览。
- 核销：只有门店绑定的管理员可以核销；校验订单 PAID、无处理中售后，核销后订单转 COMPLETED 并写收货时间与日志。
- 小程序 `/trade/app/delivery/pick-up-stores` 返回开启门店；订单详情新增门店名称、电话、地址和核销码。
- 新增后台自提门店页面与菜单 `trade/pick-up-store/index`；小程序新增自提门店页和商城入口。

## 验证

- trade 全模块 **261 项**测试通过，0 失败/错误/跳过；新增 `PickUpPersistenceTest` 3、`PickUpMenuSeedTest` 1，Checkout 100 项回归通过。
- React **312 项**、小程序 **175 项**、网关 **35 项**测试通过。
- 真实环境：
  - 创建门店 `2116709400812654594`，门店列表返回开启门店。
  - 订单 `TRABDC9A309C5946E3A83FACFD` 设置为自提，生成核销码 `48764557`。
  - 店员切换为管理员 2 后管理员 1 核销被拒绝；恢复绑定管理员 1 后核销成功，订单转 `COMPLETED`、写入收货时间，订单详情返回门店与核销码。

证据：`evidence/pickup-store-verification.json`。

## 未覆盖

- 买家结算时选择自提门店、按距离排序和自提运费规则。
- 自提订单支付后的“待自提”独立状态与发货语义。
- 核销小票、门店库存、店员端小程序。
- 浏览器后台与小程序真机联调。
- 本批未 commit/push。
