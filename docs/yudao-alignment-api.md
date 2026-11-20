# 商城、交易、会员业务收敛接口变更

2026-10-28。以当前代码为准，下面是构造示例，不代表已部署或渠道实测。参考芋道固定版本 8db9153bfe6bdc03baf93b74e244a2e74477c5c4，保持本项目 app/web 分层与服务边界。

## 活动管理

后台使用管理员会话及对应权限。满减路径 /mall/promotion/reward-activities，折扣路径 /mall/promotion/discount-activities。两类独立请求和响应。

| 方法 | 路径后缀 | 权限 | 行为 |
| --- | --- | --- | --- |
| POST | 无 | mall:promotion:create | 新增，返回 R<Long> |
| PUT | /{id} | mall:promotion:update | 修改，返回 R<Boolean> |
| PUT | /{id}/close | mall:promotion:update | 关闭，返回 R<Boolean> |
| DELETE | /{id} | mall:promotion:delete | 删除已关闭活动 |
| GET | /{id} | mall:promotion:query | 详情 |
| GET | 无 | mall:promotion:list | name/status/pageNum/pageSize 分页 |

活动新增即启用，status=0；关闭后 status=1，不能编辑或重新开启。计价仍检查开始与结束时间。同一 SKU 不能同时属于两个启用的折扣活动；满减检查时间及商品范围冲突。移除发布版本、发布接口和限次计数。

满减新增：
```json
{"name":"满百减十","startTime":"2026-11-01 00:00:00","endTime":"2026-12-01 00:00:00","conditionType":"PRICE","productScope":"ALL","productScopeValues":[],"rules":[{"limit":"100.00","discountPrice":"10.00"}]}
```
conditionType 为 PRICE（金钱）或 COUNT（件数）；productScope 为 ALL、SPU_IDS 或 CATEGORY_IDS。规则按门槛递增，使用满足的最高档。

折扣新增：
```json
{"name":"商品八折","startTime":"2026-11-01 00:00:00","endTime":"2026-12-01 00:00:00","products":[{"spuId":"1","skuId":"2","discountType":"PERCENT","discountPercent":8000}]}
```
PERCENT 为 1..9999 基点，8000 表示八折；PRICE 使用 discountPrice 表示每件减价，单位元，必须小于原价。

## 商品、结算与售后

SPU 修改提交完整 skus 集合。已有 SKU 带 id，新增不带；不允许改已有编码，省略的旧 SKU 移除（有预占不能移除）。独立 SKU 编辑和库存调整接口退役。

会员结算报价新增可选 pointStatus，默认 false。返回 usePoint/givePoint。商品折扣与会员折扣取更优惠，再满减、优惠券、积分；金额始终由服务端计算。商品行保存实付金额，报价摘要包含积分和行实付，客户端不能篡改订单金额。

退款只用 POST /trade/admin/after-sales/{no}/refund（trade:after-sale:refund）；原 refund-requests 退款入口退役。退款金额、会员和原支付单均由售后记录确定。必须先同意退款；退货退款必须先确认收货。支付退款成功后回补售后商品库存，整单退款归还券，积分和经验按原交易记录回退。重复同售后请求复用原退款单及权益记录。

后台代会员购物、直接创建会员、重置会员密码、直接调整经验及 memberNo 返回字段已移除。会员主键为唯一业务识别编号。历史文档中对应旧接口不再有效。

## 错误与验收边界

复用 R、PageResult 和统一异常处理。参数 Bean Validation 失败返回框架校验错误；业务失败检查 R.code，不能只依据 HTTP 200 判断成功。金额为精确元字符串，ID 按字符串处理避免精度损失。

真实支付、退款和物流 SDK 后续接入；本次通过本地持久化、HTTP 契约和模拟远程边界测试验证业务，不代表供应商端到端验收。数据库升级说明见 yudao-alignment-upgrade.md。
