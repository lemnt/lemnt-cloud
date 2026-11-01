# 分销接口

2026-11-01。分销金额为元；佣金记录状态 0 待结算、1 已结算、2 已取消；提现状态 0 审核中、10 审核通过、11 提现成功、20 审核不通过、21 提现失败。

## 会员端

| 方法与路径 | 功能 |
| --- | --- |
| GET /trade/app/brokerage/user | 本人分销资格、可用/冻结佣金、推广员和一级/二级人数 |
| POST /trade/app/brokerage/bind | 绑定推广员，body `{"bindUserId":2}` |
| GET /trade/app/brokerage/records | 本人佣金记录，支持 status、pageNum、pageSize |
| GET /trade/app/brokerage/withdraws | 本人提现记录，支持 status、pageNum、pageSize |
| POST /trade/app/brokerage/withdraws | 申请提现：`price`、`type`（1 钱包、2 银行卡、3 微信收款码、4 支付宝收款码）及对应收款信息 |

绑定不能指向自己或形成环；已绑定后只有配置 `brokerage_bind_mode=3` 允许覆盖。提现金额不得低于配置最低金额、不得超过可用佣金；手续费按配置比例向下取整到分。

## 管理端

| 方法与路径 | 权限 | 功能 |
| --- | --- | --- |
| GET /trade/admin/brokerage/users | trade:brokerage:list | 分销用户分页 |
| PUT /trade/admin/brokerage/users/{id}/enabled | trade:brokerage:update | 开通/取消分销资格 |
| PUT /trade/admin/brokerage/users/{id}/bind | trade:brokerage:update | 修改/清除推广员 |
| GET /trade/admin/brokerage/records | trade:brokerage:list | 佣金记录分页 |
| GET /trade/admin/brokerage/withdraws | trade:brokerage:list | 提现单分页 |
| POST /trade/admin/brokerage/withdraws/{id}/audit | trade:brokerage:audit | 审核通过或驳回 |
| POST /trade/admin/brokerage/unfreeze | trade:brokerage:update | 手动触发到期佣金解冻 |

## 规则

- 订单支付成功后按 `trade_config.brokerage_first_percent` / `brokerage_second_percent` 向买家的推广员及其上级计佣；冻结天数大于零进入待结算，否则直接可用。
- 整单退款后取消该订单佣金；部分退款暂不回收。
- 到期佣金由调度器每分钟解冻，`POST /unfreeze` 可手动触发。
- 钱包提现审核通过后将“提现金额 - 手续费”写入会员钱包，流水类型 `BROKERAGE_WITHDRAW`；银行卡/收款码为人工到账，审核通过即完成。驳回返还全部提现金额。
