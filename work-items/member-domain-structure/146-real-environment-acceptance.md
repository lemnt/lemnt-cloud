# 促销活动与 0 元积分订单真实环境联调验收

2026-11-01 第146批。在独立真实环境验证第141–145批的促销运营、活动下单、0 元积分支付/退款和活动补偿链路。本批只做验收，不修改业务代码，未 commit/push。

## 环境

| 组件 | 位置 |
| --- | --- |
| MySQL 8.0 | Docker `mysql8`，127.0.0.1:3306，独立库 `lemnt_mall` / `lemnt_trade` / `lemnt_member` / `lemnt_system` |
| Redis 7.2 | Docker `redis7`，127.0.0.1:6379，密码 123456 |
| Nacos 3.2.1 | Docker `nacos-server`，127.0.0.1:8848，命名空间 `dev` |
| MinIO | Docker `minio`，127.0.0.1:9000 |
| RocketMQ | 远端 `192.168.150.240:9876` |
| 服务 | system 9100、auth 9200、member 9110、mall 9120、trade 9130、gateway 8080 |
| JDK | Temurin 17 |

Nacos 公共配置 `lemnt-common.yaml` 补充 `spring.redis` 后，member/mall 才能正常启动；这是当前环境配置，不是代码变更。

为完成砍价/秒杀下单，环境额外准备了：

- 会员 1/2/3（手机号 13800000901/902/903，密码沿用验收密码）；
- 会员 1 收货地址 id=1；
- 商品 SPU 1 / SKU 1 配置 `delivery_types=[1]`、`delivery_template_id=1`；
- 交易运费模板 id=1（按件计费，`area_ids=[1]`，首件 1 件 0 元）。

## 已验证

### 0 元积分多商品订单与部分/整单退款

订单 `TR808DCA436A614E7F89D7112E`：

- 下单后 trade 订单 `total_amount=0.00`、`use_point=200`，状态直接 `PAID`；商城活动订单 `PAID`；两行积分 100/100。
- 第 1 次按行部分售后 1 件：生成 0 元 `POINT` 成功退款 `PR57A998C57E9144F984A5A22A2222E4E1`（`payment_no` 为空），订单保持 `PAID`，活动总库存 7→8（行 1 恢复至 5，行 2 仍为 3），会员积分 9800→9900。
- 第 2 次退剩余 2 件：生成 0 元 `POINT` 成功退款 `PR082493A8410B4BB488015FF2A9D93B52`，订单转 `REFUNDED`，商城活动订单 `REFUNDED`，活动两个 SKU 库存恢复到 5/5，会员积分回到 10000，经验为 0。
- 相同 `requestNo` 重放返回原订单；重复退款请求未重复退积分。

### 活动订单取消补偿

- 积分商品现金订单 `TR263328F3411540559CD41E9F` 取消后 trade 为 `CLOSED`；RocketMQ 补偿为异步，稍后复查商城活动订单已置 `CLOSED`，活动库存恢复到 3/3，确认不是丢失。
- 秒杀订单 `TREBDDEC62D13546B79532A056` 取消后，trade 与商城活动订单均为 `CLOSED`，秒杀库存由 8 恢复到 10。
- 拼团订单 `TR2EC65F20549141009CDEAD5D` 取消后，trade 与商城活动订单均为 `CLOSED`。

### 砍价

活动 `2116575623813746689`（首价 10.00、底价 8.00、库存 5、助力上限 2）：

- 会员 1 建记录 `2116575647591256066`，会员 2 助力 1.00 后价格 9.00；
- 会员 3 再助力 1.00，价格 8.00、记录状态 `SUCCESS`、助力记录 2 条；
- 会员 1 下单 `TR8F5743FE0EE246BF95D07485`，金额 8.00、`PENDING_PAYMENT`；商城活动订单 `RESERVED`，砍价库存 5→4，砍价记录已绑定订单。

### 秒杀

时段 `2116576451366703105`、活动 `2116576463161085954`（秒杀价 4.00、库存 10）：

- `GET /mall/app/seckill/configs`、`/now`、`/activities` 均返回时段与活动；
- 会员 1 下单 2 件 `TREBDDEC62D13546B79532A056`，金额 8.00，秒杀库存 10→8，活动订单 `RESERVED`；
- 相同 `requestNo` 重放返回同一订单号。

### 拼团

活动 `2116576587669000193`（拼团价 6.00、成团人数 2、虚拟成团）：

- `GET /mall/app/combination/activities`、`/activities/{id}`、`/summary` 均正常；
- 会员 1 下单 1 件 `TR2EC65F20549141009CDEAD5D`，金额 6.00；活动订单 `RESERVED`，`activity_record_id` 为空。

## 未覆盖

- 秒杀/拼团/砍价已支付链路：当前没有支付提交与真实渠道 SDK，拼团支付后建团、成团人数与虚拟成团未验证（已支付链路于第147批用沙箱渠道补齐并验收，见[147-payment-channel-delivery.md](147-payment-channel-delivery.md)）。
- 浏览器后台页面与小程序真机：仅验证后端 API 与数据库状态。
- 支付渠道退款：0 元退款只验证 `POINT` 权益回退，不是渠道退款。
- 部分失败场景（Nacos 断连、Redis 故障、MQ 不可用）未做故障演练。

## 证据

- `evidence/real-environment-acceptance.json`

## 限制说明

- 取消补偿为异步，验收时需等待 MQ 消费后再判定；短时间内的 `RESERVED` 不代表丢失。
- 本批未 commit/push，未修改业务代码。
