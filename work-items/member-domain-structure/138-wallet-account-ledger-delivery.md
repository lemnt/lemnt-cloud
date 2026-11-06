# 会员钱包账户与余额流水交付与验证

2026-10-31 第138批。按设计交付会员钱包账户、余额流水、冻结/解冻、后台人工调整，以及后台与 App 查询。金额单位为元（DECIMAL(19,2)），参考芋道钱包模型并适配项目逐表 DO/Gateway 约定。充值套餐/充值退款与转账留待后续批次。

## 后端

新增 `trade_member_wallet`、`trade_member_wallet_transaction` 两表（`V12__member_wallet.sql`），成员唯一账户、流水唯一 `(wallet_id,biz_type,biz_no)`。

- 领域：`Wallet`、`WalletTransaction`、`WalletBizType`、筛选、`WalletGateway`；业务类型 RECHARGE/RECHARGE_REFUND/PAYMENT/PAYMENT_REFUND/UPDATE_BALANCE/TRANSFER。
- 基础设施：DO/Mapper/Converter/GatewayImpl；资金变更使用 `SELECT ... FOR UPDATE` 行锁。
- 应用 `WalletService`：
  - 入账 credit / 出账 debit（余额不足拒绝）/ 冻结 freeze / 解冻 unfreeze / 人工调整 adjust（正增负减）。
  - 事务内锁钱包行，按 `(wallet_id,biz_type,biz_no)` 查流水实现幂等；金额不一致拒绝；冻结/解冻不写收支流水。
  - 调整必须携带 requestNo 作为业务号，重复提交不重复记账。
- 后台接口 `/trade/admin/wallets`：分页、详情、流水、调整；权限 `trade:wallet:list/query/adjust`；菜单种子 `V20261031__trade_wallet_menu.sql`。
- App 接口 `/trade/app/wallet`：本人钱包（首次访问创建零余额账户）、本人流水；身份只取认证上下文。

## 后台 UI 与小程序

- `walletApi.ts` 严格解码（大编号、非负余额、带符号流水金额）；`WalletPage.tsx` 列表、详情+流水抽屉、调整表单（requestNo 预填、正负金额、原因）。
- 路由 `trade/wallet/index`；菜单 component 一致。
- 小程序 `trade.js` 增加 `wallet`/`walletTransactions`；新增 `pages/member-wallet/index`，会员中心增加“我的钱包”入口。

## 验证

- 后端 trade 全模块 **206 项通过**、0 失败/错误/跳过：domain 2、application 43、infrastructure 10、adapter 25、start 126。
  - 新增 `WalletServiceTest` 7：入账、出账不足、支出/累计、冻结解冻、幂等与金额冲突、调整符号/零/精度、非法会员与金额。
  - 新增 `WalletPersistenceTest` 2：生产 SQL/DO/Mapper/Gateway + H2，成员唯一账户、行锁查询、余额更新、流水唯一与分页。
  - 新增 `WalletMenuSeedTest` 1；`ControllerMappingTest` 收录新路由。
- 前端 **277 项通过**（新增 `walletApi.test.ts` 3），`typecheck`、`lint`、`format:check` 通过。
- 小程序 **138 项通过**（新增 `wallet.test.cjs` 2）。
- 证据：`evidence/wallet-account-ledger-verification.json`。

## 限制

- 未执行真实 MySQL/Nacos/Redis/Dubbo/浏览器/真机联调；H2 与单测不等于真实环境验收。
- 充值套餐/充值单/充值退款（Y06）与转账（Y07）未在本批，后续基于本批 `credit/debit/freeze/unfreeze` 扩展。
- 未主动 commit/push。
