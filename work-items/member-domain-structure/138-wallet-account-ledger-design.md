# 会员钱包账户与余额流水设计

2026-10-31 第138批。对应完整迁移 F09 的钱包账户/流水部分与台账 Y05。参考芋道 `PayWalletDO`/`PayWalletTransactionDO`，适配当前项目金额单位（元，BigDecimal）与逐表 DO/Gateway 约定。充值套餐/充值退款与转账分别留待后续批次。

## 范围

- 会员钱包账户：余额、冻结金额、累计支出、累计充值。
- 余额流水：业务类型、业务单号、标题、带符号金额、交易后余额。
- 操作：入账（credit）、出账（debit）、冻结（freeze）、解冻（unfreeze）、后台人工调整（adjust）。
- 后台：钱包分页/详情/流水，人工调整；App：本人钱包与流水。
- 不包含：充值套餐/充值单/充值退款、转账、真实支付渠道。

## 模型

`trade_member_wallet`（member_id 唯一）：
- balance、freeze_price、total_expense、total_recharge，DECIMAL(19,2) 元；
- 审计列；不逻辑删除（账户长期保留）。

`trade_member_wallet_transaction`（追加式）：
- transaction_no、wallet_id、biz_type、biz_no、title、amount（正增负减）、balance_after；
- 审计列；唯一 (wallet_id, biz_type, biz_no) 保证同一业务只入账一次。

业务类型：RECHARGE、RECHARGE_REFUND、PAYMENT、PAYMENT_REFUND、UPDATE_BALANCE、TRANSFER。

## 并发与幂等

- 每次资金变更在事务内 `SELECT ... FOR UPDATE` 锁定钱包行，串行化同一会员的资金操作；行锁由数据库保证，不引入 Redis 锁。
- 入账/出账/调整先按 (wallet_id,biz_type,biz_no) 查既有流水：存在且金额一致则直接返回，不一致则报错；不存在再更新余额并追加流水。
- 出账/冻结校验可用余额足够，失败抛出业务异常，不产生流水。
- 冻结/解冻只调整 balance 与 freeze_price，不写流水（与源一致：冻结不是余额收支）。
- 人工调整必须携带 requestNo 作为 biz_no，保证重复提交不重复记账。

## 接口

后台：
- `GET /trade/admin/wallets`（memberId/pageNum/pageSize）`trade:wallet:list`
- `GET /trade/admin/wallets/{memberId}` `trade:wallet:query`
- `GET /trade/admin/wallets/{memberId}/transactions`（bizType/pageNum/pageSize）`trade:wallet:query`
- `POST /trade/admin/wallets/{memberId}/adjust`（requestNo/amount/reason）`trade:wallet:adjust`

App（本人身份）：
- `GET /trade/app/wallet`
- `GET /trade/app/wallet/transactions`（pageNum/pageSize）

## 分层

- 领域：Wallet、WalletTransaction、WalletBizType、筛选、WalletGateway。
- 基础设施：DO/Mapper/Converter/GatewayImpl，行锁查询 `lockByMemberId`。
- 应用：WalletService（事务、锁定、幂等、入账/出账/冻结/解冻/调整）、DTO/查询/执行器。
- 适配：后台与 App 控制器，独立 Req/Res。
- 前端：后台钱包页 + 流水抽屉，App 钱包页；菜单种子。

## 验证

- 应用层：入账、出账不足、冻结/解冻、同业务幂等、金额不一致冲突、调整符号与 requestNo。
- 持久化：生产 SQL/DO/Mapper/Gateway + H2，唯一账户、行锁、流水唯一、累计字段。
- 路由映射与菜单种子。
- 前端严格解码与页面。
- 不执行真实 MySQL/Nacos/Redis/浏览器联调。
