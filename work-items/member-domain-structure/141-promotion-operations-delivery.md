# 促销运营域迁移交付（秒杀/拼团/砍价/积分商城）

2026-10-31 第141批。按 F07 迁移商城促销运营四个子域：秒杀 A04、拼团 A05、砍价 A06、积分商城 A07。本批完成各子域后端领域/接口、后台管理页、小程序浏览与参与页、SQL 与菜单种子；活动下单与订单处理器仍留到订单集成批次，明细见下方“未完成”。

## 后端

### 表

| 文件 | 表 |
|---|---|
| `lemnt-mall/sql/V18__promotion_seckill.sql` | `mall_promotion_seckill_config`、`mall_promotion_seckill_activity`、`mall_promotion_seckill_product` |
| `lemnt-mall/sql/V19__promotion_combination.sql` | `mall_promotion_combination_activity`、`mall_promotion_combination_product`、`mall_promotion_combination_record` |
| `lemnt-mall/sql/V20__promotion_bargain.sql` | `mall_promotion_bargain_activity`、`mall_promotion_bargain_record`、`mall_promotion_bargain_help` |
| `lemnt-mall/sql/V21__promotion_point.sql` | `mall_promotion_point_activity`、`mall_promotion_point_product` |

金额沿用项目元单位 `DECIMAL(19,2)`；活动时段编号使用逗号存储，查询采用 `CONCAT(',',config_ids,',') LIKE ...` 兼容 H2/MySQL；库存扣减使用条件 `setDecrBy`，活动行锁放 XML。

### 秒杀

- 时段 CRUD/状态/分页/列表/精简列表；时间点 `HH:mm:ss`，重叠校验，被活动引用时拒绝删除。
- 活动 CRUD/关闭/删除/分页；商品必须属于活动 SPU，秒杀价低于原价，同一商品在同一时段只能参加一个开启活动，活动库存为商品库存合计，修改时总库存不回退。
- App：时段列表、当前时段活动、活动分页、详情（当天时段换算）。
- `SeckillStockService`：下单前校验活动状态、时段、单次限购、商品库存，并支持活动与商品库存扣减/恢复（活动或商品删除后恢复跳过，不阻断取消）。

### 拼团

- 活动 CRUD/关闭/删除/分页：成团人数、虚拟成团、限制时长、限购校验，价格低于原价。
- 记录：参团校验（父团状态/人数/过期、本人进行中团、总限购）、开团/参团、满员整团成功、订单号绑定；虚拟成团补齐占位记录后成功，非虚拟团过期统一失败。订单支付后建团由订单批次调用 `CombinationRecordService.create`。
- App：活动分页/详情、可参与的团长记录、本人记录分页/详情、拼团概要。
- 调度：`CombinationExpireScheduler` 默认关闭，`lemnt.mall.scheduler.enabled=true` 时每分钟处理到期团。

### 砍价

- 活动 CRUD/关闭/删除/分页：单一 SKU、起始价/底价、随机砍价区间、助力人数、帮砍次数、限购和库存校验。
- 记录：发起砍价（进行中唯一、成功限购）、好友帮砍（不能自砍、每人限次、重复助力拒绝、按旧价 CAS 更新避免并发覆盖）、砍到底价或助力人数达标即成功；活动结束后进行中记录失败；支持砍价成功后下单校验与订单号绑定。
- App：活动分页/详情、最近记录、发起砍价、本人记录、详情（助力列表/本人助力状态）、帮砍。
- 调度：`BargainExpireScheduler` 默认关闭，开启调度后每分钟处理到期记录。

### 积分商城

- 活动 CRUD/关闭/删除/分页：商品 SKU 校验、兑换次数/积分/补差金额/库存校验，同一商品只能参加一个开启活动；关闭同步商品快照状态。
- App：活动分页、详情（展示积分最少商品）。
- `validateJoin` 与活动/商品库存扣减/恢复，供订单兑换调用。

### 接口

后台（均复用 `mall:promotion:*` 权限）：

```
GET    /mall/promotion/seckill-configs
GET    /mall/promotion/seckill-configs/{id}
POST   /mall/promotion/seckill-configs
PUT    /mall/promotion/seckill-configs/{id}
PUT    /mall/promotion/seckill-configs/{id}/status
DELETE /mall/promotion/seckill-configs/{id}
GET    /mall/promotion/seckill-configs/list
GET    /mall/promotion/seckill-configs/simple-list
GET    /mall/promotion/seckill-activities
GET    /mall/promotion/seckill-activities/{id}
POST   /mall/promotion/seckill-activities
PUT    /mall/promotion/seckill-activities/{id}
PUT    /mall/promotion/seckill-activities/{id}/close
DELETE /mall/promotion/seckill-activities/{id}
GET    /mall/promotion/combination-activities
GET    /mall/promotion/combination-activities/{id}
POST   /mall/promotion/combination-activities
PUT    /mall/promotion/combination-activities/{id}
PUT    /mall/promotion/combination-activities/{id}/close
DELETE /mall/promotion/combination-activities/{id}
GET    /mall/promotion/combination-records
GET    /mall/promotion/bargain-activities
GET    /mall/promotion/bargain-activities/{id}
POST   /mall/promotion/bargain-activities
PUT    /mall/promotion/bargain-activities/{id}
PUT    /mall/promotion/bargain-activities/{id}/close
DELETE /mall/promotion/bargain-activities/{id}
GET    /mall/promotion/bargain-records
GET    /mall/promotion/bargain-helps
GET    /mall/promotion/point-activities
GET    /mall/promotion/point-activities/{id}
POST   /mall/promotion/point-activities
PUT    /mall/promotion/point-activities/{id}
PUT    /mall/promotion/point-activities/{id}/close
DELETE /mall/promotion/point-activities/{id}
```

小程序：

```
GET  /mall/app/seckill/configs
GET  /mall/app/seckill/now
GET  /mall/app/seckill/activities
GET  /mall/app/seckill/activities/{id}
GET  /mall/app/combination/activities
GET  /mall/app/combination/activities/{id}
GET  /mall/app/combination/activities/{id}/heads
GET  /mall/app/combination/records
GET  /mall/app/combination/records/{id}
GET  /mall/app/combination/summary
GET  /mall/app/bargain/activities
GET  /mall/app/bargain/activities/{id}
GET  /mall/app/bargain/activities/{id}/records
POST /mall/app/bargain/records
GET  /mall/app/bargain/records
GET  /mall/app/bargain/records/{id}
GET  /mall/app/bargain/records/{id}/helps
POST /mall/app/bargain/helps
GET  /mall/app/point-mall/activities
GET  /mall/app/point-mall/activities/{id}
```

## 后台 UI

- `SeckillPage.tsx`：秒杀时段/活动两页；时段每日时间点、轮播图多行地址；活动支持 SPU/SKU 选择、时段多选、限购、秒杀价和库存。
- `CombinationPage.tsx`：拼团活动与只读拼团记录；活动支持成团人数、虚拟成团、时限、限购和 SKU 拼团价。
- `BargainPage.tsx`：砍价活动与只读记录/助力弹窗；活动支持起始价/底价、随机区间、助力人数和帮砍次数。
- `PointPage.tsx`：积分商城活动；SKU 可单独设置兑换次数、积分、补差金额和库存。
- `routes.tsx` 注册 5 个新页面；菜单脚本 `V20261031__mall_{seckill,combination,bargain,point}_menu.sql`，可重复执行且不授予角色。

## 小程序

- 新增 `services/{seckill,combination,bargain,point}.js`，严格解码编号、金额、状态，写操作携带会员令牌。
- 新增页面：秒杀列表/详情、拼团列表/详情/我的拼团、砍价列表/详情/我的砍价、积分商城列表/详情。
- 秒杀/拼团/积分兑换详情中的下单按钮明确提示“下单链路待订单批次接入”，不伪造下单成功。

## 验证

- 后端 mall 全模块 **154 项通过**、0 失败/错误/跳过：
  - `SeckillPersistenceTest` 6：时段冲突、库存合计、同一商品时段冲突、限购/库存、App 查询、删除后恢复跳过。
  - `CombinationPersistenceTest` 4：开团/参团成团、重复参团与限购、非虚拟过期失败、虚拟成团占位。
  - `BargainPersistenceTest` 4：助力到底价成功、自砍/重复助力/进行中重复、活动到期失败、库存增减。
  - `PointPersistenceTest` 4：库存合计与最低积分、兑换校验、双表库存、关闭快照与删除。
  - 新增 4 个菜单种子测试；`ControllerMappingTest` 收录全部新路由。
- 前端 **292 项通过**（54 个文件）：新增 `seckillApi`/`combinationApi`/`bargainApi`/`pointApi` 各 2-3 项；`typecheck`、`lint`、`format:check` 通过。
- 小程序 **153 项通过**：新增秒杀 3、拼团 3、砍价 3、积分 2。
- 证据：`evidence/promotion-operations-verification.json`。

## 未完成与限制

- **订单集成未做**：`trade_order` 尚无活动类型/活动编号字段，`OrderCreateBo` 未携带活动信息；秒杀/拼团/砍价/积分兑换的统一“活动下单、支付后建团/核销、取消恢复库存/积分”仍在后续订单批次。当前后端已提供下单前校验与库存扣减/恢复服务，App 下单按钮仅提示待接入。
- 拼团虚拟成团使用占位会员编号 0 与昵称“虚拟用户”，不发送订阅消息；真实微信订阅消息未接入。
- 砍价为应用内随机金额，未做防刷/风控；真实环境并发仍需 MySQL 验证。
- 后台活动图片/轮播仅保存地址，未接入文件上传；小程序未做分享卡片。
- 未执行真实 MySQL/Redis/Nacos/网关/浏览器/真机联调。本会话未执行 commit/push，工作区仍有未提交改动。

## 142 后续交付

第142批已补齐活动订单集成：新增 `mall_promotion_order`/`mall_promotion_order_refund`，四个活动下单接口，交易 reserved/paid/closed/refunded 回调接入活动库存预占/恢复、拼团支付建团和砍价订单绑定。上文“订单集成未做”不再是当前缺口；0 元积分兑换、多 SKU/非默认地址选择、拼团整单退款失败团仍待后续，见[142 交付](142-promotion-order-integration-delivery.md)。
