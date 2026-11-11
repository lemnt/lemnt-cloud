# 分销关系、佣金与提现迁移交付

2026-11-01 第152批。营销扩展第三批：迁移分销用户关系、两级订单佣金、冻结解冻、退款取消与提现审核。后台分销页面、小程序分销中心已接通；App 活动聚合仍未开始。

## 数据

- `trade_brokerage_user`：会员编号为主键、推广员、绑定时间、分销资格、可用佣金、冻结佣金。
- `trade_brokerage_record`：订单佣金、提现扣减、提现驳回返还；按 `(user_id,biz_type,biz_id)` 唯一。
- `trade_brokerage_withdraw`：提现金额、手续费、方式、收款信息、审核状态。
- 分销开关、模式、两级比例、冻结天数、最低提现、手续费和提现方式沿用既有 `trade_config` 分销字段。
- SQL：`lemnt-server/lemnt-trade/sql/V15__brokerage.sql`。

## 规则

- 绑定推广员：不能自绑、不能形成环；已绑定时只有 `bind_mode=3` 可覆盖；推广员必须已开通资格。
- 订单支付成功后按一级/二级比例计算佣金；比例按向下取整到分；冻结天数大于零进入待结算和冻结佣金，否则直接可用。
- 到期解冻由调度器每分钟扫描，也提供后台手动触发；单条失败不阻塞其他记录。
- 整单退款后取消该订单佣金：待结算扣冻结、已结算扣可用；部分退款暂不回收佣金。
- 提现申请校验资格、最低金额、可用佣金、方式和收款信息；先扣可用佣金并写负值记录，审核通过后钱包入账（手续费扣除），驳回返还佣金并写返还记录。

## 接口

- App：`GET /trade/app/brokerage/user`、`POST /bind`、`GET /records`、`GET/POST /withdraws`。
- 后台：`/trade/admin/brokerage/users` 分页与资格/推广员修改、`/records`、`/withdraws`、`/withdraws/{id}/audit`、`/unfreeze`。
- 接口说明：`docs/brokerage-api.md`。

## 后台与小程序

- 后台 `BrokeragePage`：分销用户、佣金记录、提现审核三个页签，支持开通/取消资格、修改推广员、审核提现和解冻到期佣金；菜单 `trade/brokerage/index`。
- 小程序 `pages/brokerage/index`：分销概要、绑定推广员、佣金记录、提现申请与提现记录；会员中心增加入口。
- 钱包流水新增 `BROKERAGE_WITHDRAW` 类型。

## 验证

- trade 全模块 **255 项**测试通过，0 失败/错误/跳过；新增 `BrokeragePersistenceTest` 4、`BrokerageMenuSeedTest` 1，`CheckoutFlowIntegrationTest` 100 项回归通过。
- React **308 项**、小程序 **168 项**测试通过。
- 真实环境：
  - 会员 1 绑定推广员 2，会员 2 绑定推广员 3；订单 `TR1DBB531265B446E797201167`（8.00）支付后会员 2 冻结 0.80、会员 3 冻结 0.40；解冻后分别转为可用。
  - 订单 `TRC5D5D06DE42344128A312EE6` 支付后会员 2 可用达到 1.60；提现 1.00（手续费 0.05）审核通过，钱包余额 0.95，流水类型 `BROKERAGE_WITHDRAW`。
  - 提现 `2116699343798661121` 驳回后可用佣金返回 1.40，并写入 `提现申请驳回` 记录。
  - 订单 `TR327B6830BDB94887819731CF` 全额退款后两条订单佣金记录转 `已取消`，冻结佣金归零，可用佣金不变。

证据：`evidence/brokerage-verification.json`。

## 未覆盖

- 多级分销树、佣金固定金额、按商品单独佣金、分销海报和排行榜。
- 微信/支付宝自动转账、提现失败重试与对账；当前钱包方式直接入账，其他方式为人工到账。
- 部分退款按比例回收佣金。
- 浏览器后台与小程序真机联调。
- 本批未 commit/push。
