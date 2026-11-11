# 自提门店与核销接口

2026-11-02：第160批统一跨夜/全天营业时间语义。

## 后台门店

| 方法与路径 | 权限 | 功能 |
| --- | --- | --- |
| GET /trade/admin/delivery/pick-up-stores | trade:pick-up-store:list | 分页 |
| GET /trade/admin/delivery/pick-up-stores/{id} | trade:pick-up-store:query | 详情 |
| POST /trade/admin/delivery/pick-up-stores | trade:pick-up-store:create | 新增 |
| PUT /trade/admin/delivery/pick-up-stores/{id} | trade:pick-up-store:update | 修改 |
| PUT /trade/admin/delivery/pick-up-stores/{id}/verify-users | trade:pick-up-store:update | 绑定核销店员 |
| DELETE /trade/admin/delivery/pick-up-stores/{id} | trade:pick-up-store:delete | 删除 |

营业时间为合法的 `HH:mm:ss`；关门早于开门表示跨夜，相同时间表示全天营业；核销店员为管理员编号，最多 20 人且不能重复；status 0 开启、1 关闭。

## 自提与核销

| 方法与路径 | 权限 | 功能 |
| --- | --- | --- |
| POST /trade/admin/orders/pick-up | trade:fulfillment:update | 将已支付订单设置为自提：`{"orderNo","storeId"}` |
| GET /trade/admin/orders/pick-up/{code} | trade:fulfillment:query | 按 8 位核销码预览 |
| POST /trade/admin/orders/pick-up/{code}/verify | trade:fulfillment:update | 店员确认核销 |

只有已支付、无处理中售后的订单可设置为自提；核销码全局唯一。核销时当前管理员必须是该门店绑定的店员，核销后订单转 `COMPLETED` 并写入收货时间。

## 小程序

| 方法与路径 | 功能 |
| --- | --- |
| GET /trade/app/delivery/pick-up-stores | 开启中的自提门店列表 |
| GET /trade/app/orders/{orderNo} | 订单详情额外返回 `pickUpStoreName`、`pickUpStorePhone`、`pickUpStoreAddress`、`pickUpVerifyCode` |

结算时选择自提门店尚未接入，当前由后台将已支付订单设置为自提。
