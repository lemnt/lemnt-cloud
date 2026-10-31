# 普通快递贯通结算与订单

2026-10-28 v1。承接配送基础接口与商城复刻基线。示例为构造数据，不代表已部署 HTTP 或真实渠道实测。当前仅实现普通快递，配送方式固定为1；自提及真实支付/退款/物流渠道后续接入。

## 1. 商品配送字段

接口地址：POST `/mall/spus`、PUT `/mall/spus/{id}`；权限沿用 `mall:spu:add`、`mall:spu:edit`，详情沿用 `mall:spu:query`。原 SPU 与完整 SKU 请求字段不变，新增下列字段，创建和修改仍是独立 Req/Res。

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| deliveryTypes | Integer[] | 是 | JSON body | 无 | 1快递/2自提；空集合表示未配置配送，不能快递下单 |
| deliveryTemplateId | Long | 包含快递时是 | JSON body | 无 | 已在交易服务配置的运费模板编号，不自动分配默认模板 |
| skus[].weight | decimal | 否 | JSON body | 未配置 | 单件重量，千克，最多12位整数、6位小数；按重量计费必须有正值 |
| skus[].volume | decimal | 否 | JSON body | 未配置 | 单件体积，立方米，最多12位整数、6位小数；按体积计费必须有正值 |

重量和体积允许使用精确十进制字符串提交，管理页面使用 stringMode，避免浮点改写。DTO/详情返回如下字段（主对象在 data 下）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| data.deliveryTypes | Integer[] | 商品配送方式 |
| data.deliveryTemplateId | Long | 运费模板编号，历史未配置可能省略 |
| data.skus[].weight | string | 单件重量，未配置可能省略 |
| data.skus[].volume | string | 单件体积，未配置可能省略 |

构造新增请求：
```http
POST /mall/spus
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{"code":"SPU-DELIVERY","name":"快递商品","categoryId":1,"brandId":1,"deliveryTypes":[1],"deliveryTemplateId":9,"skus":[{"code":"SKU-DELIVERY","name":"标准款","price":"10.00","stock":10,"properties":{},"weight":"1.250000","volume":"0.125000"}]}
```

详情返回示例：
```json
{"code":200,"data":{"id":1,"code":"SPU-DELIVERY","name":"快递商品","categoryId":1,"brandId":1,"status":"OFF_SHELF","deliveryTypes":[1],"deliveryTemplateId":9,"skus":[{"id":5,"spuId":1,"code":"SKU-DELIVERY","name":"标准款","price":"10.00","stock":10,"properties":{},"status":"ON_SHELF","weight":"1.250000","volume":"0.125000"}]}}
```

后台运费模板选择读取 GET `/trade/admin/delivery/templates/simple`，需要 `trade:delivery-template:query` 权限；列表从交易服务读取，不在前端编造模板。

## 2. 本人结算预览

接口地址：`/mall/app/checkout/quote`。请求方式：POST。权限：已登录会员，会话身份来自网关，不能提交 memberId 决定身份。

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| productIds | Long[] | 是 | JSON body | 无 | 本人已勾选购物车 SKU，1至100项，不重复 |
| couponId | Long | 否 | JSON body | 不使用券 | 本人优惠券实例编号 |
| pointStatus | Boolean | 否 | JSON body | false | 是否用积分抵扣，现已贯通 Req→Cmd→Qry |
| addressId | Long | 是 | JSON body | 无 | 当前会员的收货地址编号；服务端校验归属 |

不接收客户端姓名、手机号、地区、详细地址、单价、优惠金额或运费作为计价依据。地址通过会员服务按 memberId+addressId 读取，商品配送信息由商城读取，运费通过交易服务配置计算。

| 响应字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 成功200 |
| data.settlementId | string | Redis结算单编号 |
| data.expiresAt | string | ISO UTC时间，预览有效期15分钟 |
| data.items | array | 商品行及不可变金额快照 |
| data.items[].productId | Long | SKU编号 |
| data.items[].productCode | string | SKU编码快照 |
| data.items[].productName | string | 商品名称快照 |
| data.items[].unitPrice | string | 单价，元 |
| data.items[].quantity | int | 数量 |
| data.items[].lineAmount | string | 商品原价合计，不含运费 |
| data.items[].payAmount | string | 行实付，含分摊运费，售后额度依据 |
| data.items[].deliveryPrice | string | 行运费，元 |
| data.originalAmount | string | 商品原价合计，元 |
| data.discountAmount | string | 商品优惠合计，元，不冲减运费 |
| data.payableAmount | string | 最终实付，元 |
| data.couponId | Long | 使用的券实例，未使用时可能省略 |
| data.usePoint | long | 使用积分 |
| data.givePoint | long | 支付后应赠积分 |
| data.delivery | object | 服务端冻结收件及运费快照 |
| data.delivery.addressId | Long | 本人地址编号 |
| data.delivery.deliveryType | int | 固定1，普通快递 |
| data.delivery.receiverName | string | 收件人 |
| data.delivery.receiverMobile | string | 收件联系电话 |
| data.delivery.receiverAreaId | Long | 收件地区编号 |
| data.delivery.receiverDetailAddress | string | 收件详细地址 |
| data.delivery.deliveryPrice | string | 订单运费，元 |

请求示例：
```http
POST /mall/app/checkout/quote
Authorization: Bearer <MEMBER_TOKEN>
Content-Type: application/json

{"productIds":[5],"addressId":7,"pointStatus":false}
```

成功响应示例（以每实付1元赠2积分的配置为例）：
```json
{"code":200,"data":{"settlementId":"12345678-1234-1234-1234-123456789abc","expiresAt":"2026-10-28T06:15:00Z","items":[{"productId":5,"productCode":"SKU-DELIVERY","productName":"快递商品","unitPrice":"10.00","quantity":2,"lineAmount":"20.00","payAmount":"26.50","deliveryPrice":"6.50"}],"originalAmount":"20.00","discountAmount":"0.00","payableAmount":"26.50","usePoint":0,"givePoint":53,"delivery":{"addressId":7,"deliveryType":1,"receiverName":"收件人","receiverMobile":"13800000000","receiverAreaId":110101,"receiverDetailAddress":"示例地址","deliveryPrice":"6.50"}}}
```

空购物车不是空报价成功，而是参数或业务校验失败。金额统一为精确元字符串，ID可能为数值或字符串，调用端应统一保存为字符串。

## 3. 提交结算单

接口地址：`/mall/app/checkout`。请求方式：POST。权限：已登录会员，沿用当前会员上下文。

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| settlementId | string | 是 | JSON body | 无 | 本人有效预览编号 |
| requestNo | string | 是 | JSON body | 无 | 幂等请求编号，最多64字符；未知结果必须原请求重试 |

不重复提交地址、金额和运费。提交时重读商品/地址/配置进行重新报价，摘要包含收件字段、运费、行实付和积分权益；变化时拒绝，不能静默换成新金额或新地址。

| 响应字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 成功200 |
| data.orderNo | string | 原交易订单号 |
| data.memberId | Long | 当前会员编号 |
| data.status | string | RESERVING/PENDING_PAYMENT及后续原订单状态 |
| data.originalAmount | string | 商品原价快照 |
| data.discountAmount | string | 商品优惠快照 |
| data.payableAmount | string | 包含运费的实付快照 |
| data.couponId | Long | 使用的券，未使用可能省略 |
| data.items | array | 字段同预览items，包含行实付和行运费 |
| data.delivery | object | 字段同预览delivery，之后修改会员地址不改变该快照 |

请求示例：
```http
POST /mall/app/checkout
Authorization: Bearer <MEMBER_TOKEN>
Content-Type: application/json

{"settlementId":"12345678-1234-1234-1234-123456789abc","requestNo":"MINI-DELIVERY-1"}
```

成功响应：
```json
{"code":200,"data":{"orderNo":"TR-EXAMPLE","memberId":1,"status":"PENDING_PAYMENT","originalAmount":"20.00","discountAmount":"0.00","payableAmount":"26.50","items":[{"productId":5,"productCode":"SKU-DELIVERY","productName":"快递商品","unitPrice":"10.00","quantity":2,"lineAmount":"20.00","payAmount":"26.50","deliveryPrice":"6.50"}],"delivery":{"addressId":7,"deliveryType":1,"receiverName":"收件人","receiverMobile":"13800000000","receiverAreaId":110101,"receiverDetailAddress":"示例地址","deliveryPrice":"6.50"}}}
```

同请求已有订单时返回原订单及原快照，不能因为后来改地址而再生成订单。运费分摊合计等于订单运费，行实付合计等于订单实付。

## 4. 发货、支付及售后

POST `/trade/admin/orders/{orderNo}/shipment`，权限 `trade:fulfillment:ship`。新订单只需 requestNo、carrierCode、trackingNo；服务端使用下单收件快照。原有 recipientName/phone/address 仅用于历史没有快照的订单补填，新订单不能通过这几个字段替换地址。

```http
POST /trade/admin/orders/TR-EXAMPLE/shipment
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{"requestNo":"SHIP-1","carrierCode":"SF","trackingNo":"TRACK-EXAMPLE"}
```

成功返回原有Shipment结构，不引入新回包：
```json
{"code":200,"data":{"orderNo":"TR-EXAMPLE","requestNo":"SHIP-1","carrierCode":"SF","trackingNo":"TRACK-EXAMPLE","recipientName":"收件人","phone":"13800000000","address":"示例地址","shippedAt":"2026-10-28 14:00:00"}}
```

支付使用包含运费的订单金额；积分抵扣在运费之前，赠送积分在运费之后。部分售后按冻结的行实付和数量计算额度，不查询当前运费模板重算旧订单。例：2件实付26.50元（含运费6.50元），退款1件当前服务计算13.25元。此轮没有新增自定义“不退运费”政策；完整售后管理契约沿用现有接口。

## 5. 业务错误与升级

统一使用现有异常处理。Bean Validation返回HTTP422/ProblemDetail；地址不属于本人、商品配送未配置或地区无规则等业务拒绝返回HTTP400；预览后商品/地址/运费改变返回HTTP409。构造冲突响应：
```json
{"type":"urn:lemnt:problem:business-rule-violation","title":"业务规则校验失败","status":409,"detail":"商品、优惠、地址或运费已变化，请重新结算","instance":"/mall/app/checkout","code":"BUSINESS_RULE_VIOLATION"}
```

计价RPC使用Integer分，商品实付汇总上限21474836.47元，超范围明确拒绝，不能截断。服务或远程查询失败不能作为零运费；缺有效重量/体积的按重/体积商品也不能下单。

新库使用已更新的商城V1/V2、交易V1及配送V7；存量库分别演练 `lemnt-mall/sql/upgrade/delivery_checkout_columns.sql`、`lemnt-trade/sql/upgrade/delivery_checkout_columns.sql`。旧商品需显式补配送方式和模板，不自动指定模板。旧订单新收件列保持NULL，历史行运费为0。

预览Redis键升级到 `mall:settlement:v2:`，旧无地址预览自然失效，客户端必须重新预览；提交回执的30天恢复索引保持原命名，不清除未确认订单。前后端需同步发布。真实数据库未执行变更，未部署，未做供应商端到端验收，未提交或推送。


规格快照更新：结算商品行properties现为propertyId/propertyName/valueId/valueName对象列表，报价指纹包含编号，配送分摊保留完整列表，具体合同见 [商品快照接口](order-product-snapshot-api.md)。
