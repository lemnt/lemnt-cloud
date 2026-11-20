# 订单改价改址与自提履约完善交付

2026-11-01 第158批。补齐后台订单调价、改址，订单中心待自提状态，门店距离排序、营业时间校验和店员核销台。

## 业务

- 后台 `POST /trade/admin/orders/{orderNo}/price-adjustment`：仅待支付订单可调价（第160批进一步限制为尚未生成支付单），新金额按原行实付比例重分摊，最后一行承担尾差，写类型2日志。
- 后台 `PUT /trade/admin/orders/{orderNo}/address`：仅未发货快递单可改址，只更新收件快照，不重算运费，写类型11日志。新增权限 `trade:order:update`。
- 会员订单中心新增派生状态 `AWAITING_PICKUP`：已支付自提单在列表、详情和状态数量中显示为待自提；内部持久化状态仍为 `PAID`，核销后转 `COMPLETED`。
- 小程序门店列表支持 `latitude/longitude`，按球面距离升序，缺少坐标门店排最后；门店页新增“按距离排序”。
- 交易建单校验门店营业时间，关门早于开门按跨夜处理；关闭中的门店拒绝自提下单。
- 新增 React 店员核销台 `trade/pick-up-verify/index`，仅需核销查询/确认权限，不暴露门店维护；菜单脚本 `V20261101__trade_pickup_verify_menu.sql`。

## 验证

- mall **190 项**、trade **272 项**、网关 **35 项**测试通过；React **315 项**、小程序 **176 项**测试通过。
- 新增 `OrderAdjustmentServiceTest`、`PickUpStoreDistanceTest`，扩展 `OrderManagementHttpTest`、`PickUpMenuSeedTest`、订单分页与控制器映射测试。
- 真实环境：
  - 订单 `TRB13740A955CB465386AB2F6F` 调价 `9.00→8.00`，商品行实付同步为 `8.00`，日志类型2；改址写入收件人/电话/地区/详细地址，日志类型11。
  - 自提单 `TR3759C6A7A75E4455974A37CF` 在订单列表 `status=AWAITING_PICKUP` 返回，状态数量 `awaitingPickup=1`；详情状态为 `AWAITING_PICKUP`。
  - 两个带坐标门店按当前位置排序返回 `0.00km`、`3.62km`；临时关闭门店后自提下单返回 400“自提门店当前未营业”；验收后删除临时门店并把自提开关恢复为0。
  - 浏览器后台：店员核销台查询核销码 `38846600` 并确认核销，订单转 `COMPLETED`；订单履约页展示改价弹窗。

证据：`evidence/order-adjust-pickup-verification.json`。

## 未覆盖

- 改价后的差额支付/退款、已支付订单调价。
- 自提状态仍为派生状态，未新增持久化状态机分支。
- 下单前按营业时间拦截报价（当前在建单阶段拦截）。
- 小程序真机定位授权与 iOS/Android/H5 运行验证。
- 本次会话未执行 commit/push；验收期间分支上出现了外部提交 `f5894b36`、`83fb7394`。
