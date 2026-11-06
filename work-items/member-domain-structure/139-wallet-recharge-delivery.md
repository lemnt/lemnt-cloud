# 充值套餐与充值单交付与验证

2026-10-31 第139批。按设计交付充值套餐 CRUD、充值单创建/到账/退款，并接入第138批钱包入账/出账。真实支付渠道 SDK 仍后置，到账由后台“确认到账”命令代替渠道回调。

## 后端

新增 `trade_pay_recharge_package`、`trade_pay_wallet_recharge` 两表（`V13__wallet_recharge.sql`）。

- 充值套餐：分页/详情/新增/修改/删除；名称唯一；删除前校验是否被充值单引用。
- 充值单：
  - 创建：校验套餐启用，快照支付/赠送/到账金额，requestNo 唯一幂等。
  - 确认到账：行锁充值单，PENDING→PAID，按充值单号入钱包（RECHARGE 流水，幂等）；已 PAID/REFUNDED 幂等返回。
  - 退款：行锁充值单，PAID→REFUNDED，按充值单号出钱包（RECHARGE_REFUND 流水，幂等）；余额不足抛错并回滚保持 PAID。
- 后台接口：`/trade/admin/recharge-packages`、`/trade/admin/wallet-recharges`；权限与菜单种子 `V20261031__trade_recharge_menu.sql`。
- App 接口：`GET /trade/app/recharge-packages`、`POST /trade/app/wallet/recharges`、`GET /trade/app/wallet/recharges`；身份只取认证上下文。

## 后台 UI 与小程序

- `rechargeApi.ts` 严格解码大编号、金额、状态；`RechargePage.tsx` 提供充值套餐 CRUD 与充值单列表/详情/确认到账/退款。
- 路由 `trade/recharge-package/index`、`trade/wallet-recharge/index`；菜单 component 一致。
- 小程序 `trade.js` 增加套餐/创建/本人充值单方法；新增 `pages/wallet-recharge/index`，钱包页增加“去充值”入口。

## 验证

- 后端 trade 全模块 **215 项通过**、0 失败/错误/跳过：domain 2、application 49、infrastructure 10、adapter 25、start 129。
  - 新增 `RechargeServiceTest` 6：创建快照/幂等/停用套餐/请求复用、到账入账与幂等、退款出账/重复退款/未到账拒绝/余额不足传播。
  - 新增 `RechargePersistenceTest` 2：生产 SQL/DO/Mapper/Gateway + H2，套餐名称唯一与启用过滤、充值单 requestNo 唯一、行锁与状态更新。
  - 新增 `RechargeMenuSeedTest` 1；`ControllerMappingTest` 收录新路由。
- 前端 **280 项通过**（新增 `rechargeApi.test.ts` 3），`typecheck`、`lint`、`format:check` 通过。
- 小程序 **140 项通过**（新增 `recharge.test.cjs` 2）。
- 证据：`evidence/wallet-recharge-verification.json`。

## 限制

- 真实支付渠道与渠道退款异步状态未接入；“确认到账”为后台命令，退款为同步出账，后续接入真实渠道时替换为回调驱动。
- 未执行真实 MySQL/Nacos/Redis/Dubbo/浏览器/真机联调。
- 转账（Y07）仍未实现。
- 未主动 commit/push。
