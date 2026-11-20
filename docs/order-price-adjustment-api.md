# 后台订单调价

2026-11-02，第160批。以下示例为构造数据；未做本轮真实网关联调。

## 1、调整待支付订单金额

接口地址：`/trade/admin/orders/{orderNo}/price-adjustment`

请求方式：`POST`

接口权限码：`trade:order:update`，需要后台登录身份。

请求参数：

| 参数名 | 类型 | 是否必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| orderNo | string | 是 | Path | 无 | 1至40位字母、数字、下划线或短横线 |
| totalAmount | decimal | 是 | Body | 无 | 新应付金额，人民币元，0.01至1000000.00，最多2位小数 |

订单必须为待支付且尚未生成任何渠道支付单；否则返回409。金额按原商品行实付比例重新分摊，末行承担尾差，写类型2操作日志。已生成支付单应取消订单后重新下单，不能通过改写本地金额覆盖渠道预支付金额。

响应参数：

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功为200 |
| msg | string / null | 成功提示可为空 |
| data | boolean | 成功为true |
| rows | array / null | 此接口不返回列表 |
| total | integer / null | 此接口不返回数量 |

请求示例：

```bash
curl -X POST 'https://<API_HOST>/trade/admin/orders/TR_EXAMPLE/price-adjustment' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -d '{"totalAmount":"8.00"}'
```

成功响应示例（空字段是否省略沿用公共序列化配置）：

```json
{"code":200,"msg":null,"data":true,"rows":null,"total":null}
```

已有支付单时 HTTP409，`Content-Type: application/problem+json`：

```json
{
  "type":"urn:lemnt:problem:business-rule-violation",
  "title":"业务规则校验失败",
  "status":409,
  "detail":"订单已生成支付单，不能调价，请取消订单后重新下单",
  "instance":"/trade/admin/orders/TR_EXAMPLE/price-adjustment",
  "code":"BUSINESS_RULE_VIOLATION"
}
```

错误响应可另含 `traceId`。订单不存在返回404，状态不允许或金额已变化返回409，Bean Validation失败返回422；权限由既有后台鉴权处理。
