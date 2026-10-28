# 优惠券规则快照接口

2026-10-28。本批按本地芋道 CouponTemplateDO/CouponDO/CouponServiceImpl、后台优惠券页面及 uni-app CouponApi 对齐。接口地址沿用当前项目，金额为元，两位小数字符串；请求金额为 JSON 数值。日期为数据库时区 `yyyy-MM-dd HH:mm:ss`，有效期左闭右开。以下请求和 JSON 为构造示例，未部署 HTTP 实测。

## 1. 创建、修改模板

接口地址：创建 `POST /mall/admin/coupon-templates`；修改 `PUT /mall/admin/coupon-templates/{id}`。
接口权限码：创建 `mall:coupon-template:add`；修改 `mall:coupon-template:edit`。修改使用独立 Req/Cmd，不改已发券；已发数量、已用数量和启停状态不允许通过请求修改。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | Bearer 后台令牌 |
| id | long/string | 修改必填 | Path | 无 | 模板编号 |
| code | string | 是 | Body | 无 | 唯一编码，最长64 |
| name | string | 是 | Body | 无 | 最长100 |
| kind | string | 是 | Body | 无 | FIXED满减、PERCENT折扣 |
| threshold | decimal | 是 | Body | 无 | 适用商品经前序优惠后金额门槛，元 |
| fixedAmount | decimal | FIXED | Body | 无 | 满减金额，其他类型不填 |
| discountPercent | integer | PERCENT | Body | 无 | 支付百分比1至100，80表示八折 |
| discountLimit | decimal | 否 | Body | 无 | 折扣优惠封顶，元，仅PERCENT |
| totalCount | integer | 是 | Body | 无 | 正整数或-1不限；USER受总量限制，ADMIN/REGISTER不受此总量限制 |
| validFrom | datetime | DATE | Body | 无 | 固定生效时间 |
| validUntil | datetime | DATE | Body | 无 | 固定到期时间 |
| description | string | 否 | Body | 无 | 使用说明，最长500 |
| takeType | string | 否 | Body | ADMIN | USER会员领、ADMIN后台发、REGISTER注册赠 |
| takeLimitCount | integer | 否 | Body | 1 | 每会员每模板限领次数，-1不限，撤回不计入 |
| productScope | string | 否 | Body | ALL | ALL、SPU_IDS、CATEGORY_IDS |
| productScopeValues | long/string[] | 指定范围必填 | Body | 空数组 | SPU或分类编号，不是SKU编号，最多1000，不重复 |
| validityType | string | 否 | Body | DATE | DATE固定日期、TERM领取后有效 |
| fixedStartTerm | integer | TERM | Body | 无 | 领取后几天生效，允许0 |
| fixedEndTerm | integer | TERM | Body | 无 | 生效后持续天数，必须大于0 |

TERM从数据库领取时刻加天数计算，不对齐自然日。模板修改后，新券使用新规则，旧券继续使用原快照。USER总量不能改为小于已发数量。

```bash
curl -X POST 'https://<API_HOST>/mall/admin/coupon-templates' \
 -H 'Authorization: Bearer <TOKEN>' -H 'Content-Type: application/json' \
 -d '{"code":"WELCOME","name":"领取后三天券","kind":"FIXED","threshold":10,"fixedAmount":2,"totalCount":-1,"description":"指定商品可用","takeType":"USER","takeLimitCount":2,"productScope":"SPU_IDS","productScopeValues":[11],"validityType":"TERM","fixedStartTerm":1,"fixedEndTerm":3}'
```

创建响应（全字段示例，null字段按当前 Jackson 配置省略）：

```json
{"code":200,"data":{"id":3,"code":"WELCOME","name":"领取后三天券","kind":"FIXED","threshold":"10.00","fixedAmount":"2.00","totalCount":-1,"issuedCount":0,"enabled":true,"description":"指定商品可用","takeType":"USER","takeLimitCount":2,"productScope":"SPU_IDS","productScopeValues":[11],"validityType":"TERM","fixedStartTerm":1,"fixedEndTerm":3,"useCount":0}}
```

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code | integer | 成功200 |
| data | object/boolean | 创建返回模板；修改返回实际更新布尔值 |
| data.id | long/string | 模板编号 |
| data.code/name/kind/threshold/fixedAmount/discountPercent/discountLimit | 对应请求类型 | 当前规则，金额返回字符串 |
| data.totalCount/validFrom/validUntil/description/takeType/takeLimitCount/productScope/productScopeValues/validityType/fixedStartTerm/fixedEndTerm | 对应请求类型 | 当前发券配置，非本类型字段可缺失 |
| data.issuedCount | integer | 已发数量，撤回扣减 |
| data.useCount | integer | 已核销数量，整单退款退券扣减 |
| data.enabled | boolean | 是否允许后续发券 |

修改响应：`{"code":200,"data":true}`。参数结构或字段约束不满足返回422；业务规则冲突返回400；错误为统一 ProblemDetail。注册赠送触发器尚待会员批次对接，提供REGISTER配置不代表注册赠送链路已完成。

## 2. 删除模板、撤回券

| 接口地址 | 请求方式 | 接口权限码 | 说明 |
|---|---|---|---|
| `/mall/admin/coupon-templates/{id}` | DELETE | mall:coupon-template:edit | 逻辑删除，不撤销已领券 |
| `/mall/admin/members/{memberId}/coupons/{couponId}` | DELETE | mall:coupon:issue | 仅AVAILABLE、EXPIRED可撤回；LOCKED、USED拒绝 |

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 后台令牌 |
| id/memberId/couponId | long/string | 对应路径必填 | Path | 无 | 模板、会员和持券编号 |

```bash
curl -X DELETE 'https://<API_HOST>/mall/admin/members/1/coupons/5' -H 'Authorization: Bearer <TOKEN>'
```

成功：`{"code":200,"data":true}`。code为成功码，data为实际执行结果。撤回失败返回400 ProblemDetail；撤回与计数更新在同一事务，重复请求不重复扣减。

## 3. 领券中心与本人领取

GET `/mall/app/coupon-center?pageNum=1&pageSize=20` 返回 `R.page`，顶层 `rows/total`。POST `/mall/app/coupon-center/{templateId}/claim` 每次成功生成一张新券，超过每人限额或公开总量返回400；不再重复返回旧券。仅允许takeType=USER且启用、未到期的模板。DATE券尚未到生效时间也可领取，使用时必须已生效。

权限：网关认证后的本人会员；不接收memberId。分页参数为Query，pageNum默认1、pageSize默认20且1至100；templateId为必填Path；Authorization为必填Header。

```bash
curl -X POST 'https://<API_HOST>/mall/app/coupon-center/3/claim' -H 'Authorization: Bearer <MEMBER_TOKEN>'
```

```json
{"code":200,"data":{"id":5,"templateId":3,"memberId":1,"status":"AVAILABLE"}}
```

响应code成功200；data.id为新持券ID，templateId来源模板，memberId本人，status为持券状态，orderNo未锁定时省略。已领券且未达到限额时claimable仍可为true；客户端刷新权威领取次数，响应丢失不能声称未领到券。

## 4. 本人券包（独立快照契约）

接口地址：`/mall/app/coupon-wallet`；请求方式：GET；权限：本人会员。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 会员令牌 |
| pageNum | integer | 否 | Query | 1 | 页码 |
| pageSize | integer | 否 | Query | 20 | 1至100 |

```bash
curl 'https://<API_HOST>/mall/app/coupon-wallet?pageNum=1&pageSize=20' -H 'Authorization: Bearer <MEMBER_TOKEN>'
```

```json
{"code":200,"rows":[{"couponId":5,"templateId":3,"couponStatus":"AVAILABLE","terms":{"code":"WELCOME","name":"领取后三天券","takeType":"USER","kind":"FIXED","threshold":"10.00","fixedAmount":"2.00","productScope":"SPU_IDS","productScopeValues":[11],"validFrom":"2026-10-29 12:00:00","validUntil":"2026-11-01 12:00:00"},"usable":false}],"total":1}
```

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code | integer | 成功200 |
| rows | array | 本人持券，不包含逻辑删除券 |
| total | long | 总条数，空页rows=[]/total=0 |
| rows[].couponId | long/string | 持券编号，同模板多券各自不同 |
| rows[].templateId | long/string | 来源模板，删除模板后仍存在 |
| rows[].couponStatus | string | AVAILABLE、LOCKED、USED、EXPIRED |
| rows[].terms | object | 发放时冻结权益；不再返回当前template对象 |
| rows[].terms.code/name/takeType/kind | string | 原编码、名称、领取类型、券类型 |
| rows[].terms.threshold/fixedAmount/discountLimit | decimal-string | 元；非本类型或无上限字段可省略 |
| rows[].terms.discountPercent | integer | 支付百分比，仅折扣券 |
| rows[].terms.productScope | string | 原适用范围 |
| rows[].terms.productScopeValues | long/string[] | 原商品或分类编号 |
| rows[].terms.validFrom/validUntil | datetime | 原生效、到期时间 |
| rows[].usable | boolean | 按持券状态和原日期判断，结算仍复核范围、门槛、占用 |

模板停用、编辑、删除都不改写券包。缺少快照的历史券返回409，要求先完成升级，不能运行期静默读取当前模板。AVAILABLE不保证已生效或未过期；过期任务只更新AVAILABLE，不解除LOCKED。退款退券、关单释放依原日期恢复AVAILABLE/EXPIRED。

## 5. 失败与运维

400业务失败构造示例：

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"超过优惠券领取上限","code":"BUSINESS_RULE_VIOLATION"}
```

422参数错误code=VALIDATION_ERROR；缺少历史快照409。具体title等公共字段由统一异常处理器产生，不自定义优惠券异常包装。认证、权限沿用网关及统一后台权限机制。

过期任务开关 `lemnt.mall.scheduler.enabled=true`；间隔 `lemnt.mall.scheduler.coupon-expire-delay-ms` 默认60000。未启用调度仍按原有效期禁止过期券参与结算。新库用初始化SQL；旧库先备份、停写并审核 `lemnt-server/lemnt-mall/sql/upgrade/coupon_rule_snapshot.sql`，本批未执行真实数据库升级。历史券原权益无法从曾改动的模板逆推，升级回填以当时模板为准，必须提前核对。

## 6. 后台发券记录分页

接口地址：`/mall/admin/coupons`；请求方式：GET；接口权限码：`mall:coupon:list`。后台页面“发券记录”使用此接口，支持按会员、模板和券状态查询及撤回。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 后台令牌 |
| memberId | long/string | 否 | Query | 无 | 会员筛选 |
| templateId | long/string | 否 | Query | 无 | 来源模板筛选 |
| status | string | 否 | Query | 无 | AVAILABLE、LOCKED、USED、EXPIRED |
| pageNum | integer | 否 | Query | 1 | 1至1000000 |
| pageSize | integer | 否 | Query | 20 | 1至100 |

```bash
curl 'https://<API_HOST>/mall/admin/coupons?memberId=1&status=USED&pageNum=1&pageSize=20' -H 'Authorization: Bearer <TOKEN>'
```

```json
{"code":200,"rows":[{"id":5,"memberId":1,"templateId":3,"status":"USED","orderNo":"ORDER1","terms":{"code":"WELCOME","name":"领取后三天券","takeType":"USER","kind":"FIXED","threshold":"10.00","fixedAmount":"2.00","productScope":"SPU_IDS","productScopeValues":[11],"validFrom":"2026-10-29 12:00:00","validUntil":"2026-11-01 12:00:00"},"useTime":"2026-10-30 13:00:00"}],"total":1}
```

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code/rows/total | integer/array/long | 统一分页；查询空结果rows=[]、total=0 |
| rows[].id/templateId/memberId | long/string | 券、来源模板、持有会员编号 |
| rows[].status | string | 持券状态 |
| rows[].orderNo | string/缺失 | 关联的锁定或已用订单 |
| rows[].useTime | datetime/缺失 | 数据库实际核销时刻；退款归还清除，旧历史不伪造时间 |
| rows[].terms及全部子字段 | object | 与第4节持券规则字段含义相同，使用本接口独立Res；旧快照缺失时可缺失，须先升级数据 |

无效筛选返回422，数据库逻辑删除的券不计入分页总数；当前模板停用/删除不隐藏原持券记录。
