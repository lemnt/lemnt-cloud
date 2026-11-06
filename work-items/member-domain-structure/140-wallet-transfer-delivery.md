# 钱包转账交付与验证

2026-10-31 第140批。按 Y07 交付会员钱包间转账，复用第138批钱包 `debit/credit` 与 `TRANSFER` 流水。真实渠道出款 SDK 仍后置，本批实现钱包互转并保留审计。

## 后端

新增 `trade_member_wallet_transfer` 表（`V14__wallet_transfer.sql`），唯一 `transfer_no`、唯一 `request_no`，转入/转出会员与钱包外键。

- `WalletTransferService.transfer`：
  - 校验转出/转入会员、不能转给自己、金额两位小数、requestNo 必填。
  - requestNo 幂等：重复请求返回原记录，内容不一致拒绝。
  - 收款方必须已有钱包（未开通拒绝）；转出方钱包按需创建。
  - 按钱包编号升序 `SELECT ... FOR UPDATE` 同时锁定双方，避免 A→B 与 B→A 并发死锁。
  - 双向流水：转出 `TRANSFER` 负值、转入 `TRANSFER` 正值，业务号=requestNo；余额不足整体回滚。
- 后台接口 `/trade/admin/wallet-transfers`（分页/详情，只读）；App 接口 `/trade/app/wallet/transfers`（创建/本人转出或转入分页）。
- 菜单种子 `V20261031__trade_wallet_transfer_menu.sql`。

## 后台 UI 与小程序

- `walletApi.ts` 增加转账严格解码；`WalletPage.tsx` 增加只读转账列表与详情抽屉，路由 `trade/wallet-transfer/index`。
- 小程序 `trade.js` 增加 `walletTransfer`/`myTransfers`；新增 `pages/wallet-transfer/index`（转入会员编号、金额、备注、本人转账记录），钱包页增加“钱包转账”入口。

## 验证

- 后端 trade 全模块 **222 项通过**、0 失败/错误/跳过：domain 2、application 54、infrastructure 10、adapter 25、start 131。
  - 新增 `WalletTransferServiceTest` 5：升序加锁、双向流水、幂等复用、内容冲突、自转/收款方缺失/非法金额。
  - 新增 `WalletTransferPersistenceTest` 1：生产 SQL/DO/Mapper/Gateway + H2，request_no 唯一与双方可见分页。
  - 新增 `WalletTransferMenuSeedTest` 1；`ControllerMappingTest` 收录新路由。
- 前端 **281 项通过**（新增转账 API 用例 1），`typecheck`、`lint`、`format:check` 通过。
- 小程序 **142 项通过**（新增 `transfer.test.cjs` 2）。
- 证据：`evidence/wallet-transfer-verification.json`。

## 限制

- 当前为**钱包间同步互转**（状态 SUCCESS），非源站“商户经渠道出款到支付宝/微信”的模式；真实渠道出款 SDK 未接入，后续可在此表基础上扩展渠道与异步状态。
- 收款方必须先开通钱包；未做会员跨服务存在性校验（以钱包是否存在为准）。
- 未执行真实 MySQL/Nacos/Redis/Dubbo/浏览器/真机联调。
- 未主动 commit/push。
