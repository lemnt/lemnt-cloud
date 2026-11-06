# 充值套餐与充值单设计

2026-10-31 第139批。对应完整迁移 F09 的充值套餐/充值单/充值退款与台账 Y06。基于第138批钱包 `credit/debit` 实现；真实支付渠道 SDK 仍按约定后置，因此“支付成功”由后台确认到账命令代替渠道回调，接口与状态机保留后续接入位置。

## 范围

- 充值套餐：名称、支付金额、赠送金额、状态（0启用/1停用）CRUD；App 查询启用套餐。
- 充值单：按套餐下单，快照支付/赠送/到账金额；状态 PENDING→PAID→REFUNDED。
- 到账确认：PENDING→PAID，同时按到账金额入钱包（RECHARGE 流水，业务号=充值单号，幂等）。
- 退款：PAID→REFUNDED，按到账金额从钱包出账（RECHARGE_REFUND 流水，业务号=充值单号，幂等）；余额不足拒绝，状态保持 PAID。
- App：查询套餐、创建充值单（requestNo 幂等）、本人充值单分页。
- 不包含：真实支付渠道、渠道退款异步状态、转账（Y07）。

## 模型

`trade_pay_recharge_package`：name、pay_price、bonus_price、status、审计列、逻辑删除；唯一 name。

`trade_pay_wallet_recharge`：
- recharge_no（唯一）、request_no（唯一，创建幂等）、wallet_id、member_id；
- package_id、pay_price、bonus_price、total_price（快照）；
- status（PENDING/PAID/REFUNDED）、channel_code、paid_time、refunded_time、审计列。

金额单位元 DECIMAL(19,2)。

## 流程与并发

- 创建：校验套餐启用，按套餐快照金额生成 PENDING；requestNo 唯一，重复提交返回原单。
- 确认到账：事务内 `SELECT ... FOR UPDATE` 锁充值单；PENDING 才转 PAID，并按 recharge_no 入钱包；已 PAID/REFUNDED 幂等返回。
- 退款：锁充值单；PAID 才转 REFUNDED，并按 recharge_no 出钱包；余额不足抛错并整体回滚（保持 PAID）；已 REFUNDED 幂等返回。
- 钱包入账/出账自身按业务号幂等，重复确认/退款不会重复记账。
- 套餐被充值单引用时禁止删除。

## 接口

后台：
- `/trade/admin/recharge-packages`：分页/详情/新增/修改/删除；权限 `trade:recharge-package:list/query/create/update/remove`。
- `/trade/admin/wallet-recharges`：分页/详情/确认到账/退款；权限 `trade:wallet-recharge:list/query/confirm/refund`。

App（本人身份）：
- `GET /trade/app/recharge-packages`：启用套餐。
- `POST /trade/app/wallet/recharges`：按套餐创建（requestNo 幂等）。
- `GET /trade/app/wallet/recharges`：本人充值单分页。

## 验证

- 应用层：套餐校验、创建幂等、到账入账与幂等、退款出账/余额不足、重复退款。
- 持久化：生产 SQL/DO/Mapper/Gateway + H2，唯一键、行锁、套餐引用保护。
- 路由映射、菜单种子、前端严格解码与页面、小程序客户端与页面。
- 不执行真实 MySQL/Nacos/Redis/浏览器/真机联调。
