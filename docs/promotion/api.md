# 促销引擎接口契约与实施状态

2026-10-27：促销引擎与持久化已迁到 mall，目录事实本地读取；当前边界以 [商城与交易服务边界](../mall-trade-boundary.md) 为准。

promotion-v1/T3 · 2026-10-26 · 实施基础35fc8ec。**第1..6项管理接口已实现并通过本地HTTP/SQL测试；第7..11项V2接口仍是拟定契约。** 未部署或生产HTTP实测。详见[management.md](management.md)。行为依据[设计](design.md)，机器契约在contracts/，样例在examples/。六列请求/三列响应表沿用code-software-engineer接口模板。

## 协议与验证

- 旧/trade/app/checkout及其Redis v1保持；V2显式选择新协议，不能混用缓存、算法或请求键。身份取会话，价格/活动ID/会员ID不接受客户端决定。V2缓存及后续表均只是设计。
- revision/expectedRevision为0..2147483647 JSON整数，初始0；旧Schema minimum1已按T2实际修正。
- ID、整数分、epoch毫秒用十进制字符串，范围0..9223372036854775807，ID/epoch须大于0；quantity/revision/version为JSON整数。优惠比例按基点，8000是支付80%。金额不以小数元传输；UI格式化时不可转double。
- 未知字段、未支持玩法/人群/范围必须400拒绝。Schema additionalProperties=false只是契约，实施须采用并测试专用严格反序列化/未知字段校验，不能假设record或Spring默认会拒绝。自定义int64格式与跨字段校验由服务端领域层再次执行。
- 活动有效窗口左闭右开、epoch UTC；既有优惠券DATETIME按旧DB时钟保留。所有示例是构造数据，不是任何真实用户、活动或HTTP结果。
- 成功包装为R：code=200、data为下述模型；null可按项目Jackson约定省略，空数组保留。新V2/promotion专用错误处理拟采用HTTP400/401/403/404/409/410/503与相同R.code，data为稳定errorCode/retryable/action/correlationId；不修改V1全局错误行为。既有网关仍可能HTTP200+R.code401/503，客户端同时检查两者。
- 指定券不达原价门槛/无效/原价下优惠不足1分直接失败；活动后不达券门槛或券优惠不足1分时该组合不参与择优，绝不静默丢券，也不为0分优惠核销券。BEST只在用户选券约束下择优；NONE仍可用指定券。
- 零元最优报价如实返回，submittable=false、blockReason=ZERO_PAY_UNSUPPORTED；不改收1分，也不生成零元订单。
- 报价阶段尚无提单意图时，FIX_INPUT可修改输入重新报价；只有已有提单意图时才要求确认原结算终止。
- 原请求优先查持久DB再读缓存；已成单不因关闭新单开关/缓存故障重新创建。恢复404只表示暂未找到，不表示已终止。需重新报价时先用原settlementId/requestNo确认终止再移除本机意图，避免延迟请求下单。
- 活动管理mutation的requestNo按管理员+编号幂等，expectedRevision为乐观锁。后台重放返回原操作回执而非最新活动状态，成功后应读取详情；暂停只影响新使用，不撤销已成单HELD。终止栅栏仍以(memberId,settlementId)唯一；未成单终止不额外占用提单requestNo，新单幂等键在成功提交后绑定。

## 接口清单

### 1. 创建活动草稿

接口地址：`/trade/admin/promotions`

请求方式：`POST`

权限：`mall:promotion:create`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |
| code | string | 是 | Body | 无 | 活动唯一编码，不可修改 |
| draft | object | 是 | Body | 无 | 完整字段见模型表及Schema |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-result | 嵌套字段完整定义见下方模型表及contracts/admin-result.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/trade/admin/promotions' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"requestNo":"ADMIN-create-001","code":"DESIGN_FULL_REDUCTION","draft":{"name":"指定商品满25减1元","type":"FULL_REDUCTION","scope":{"kind":"SKU_IDS","ids":["101","102"]},"audience":"ALL_AUTHENTICATED","couponPolicy":"ALLOW_ONE","priority":10,"startsAtEpochMs":"1792886400000","endsAtEpochMs":"1793491200000","globalUseLimit":"10000","memberUseLimit":"2","rule":{"tiers":[{"thresholdFen":"2500","discountFen":"100"}]}}}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "id": "9",
    "revision": 0,
    "state": "DRAFT",
    "activeVersion": null
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 2. 保存新草稿

接口地址：`/trade/admin/promotions/{id}/draft`

请求方式：`PUT`

权限：`mall:promotion:update`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| id | string | 是 | Path | 无 | 正整数活动ID |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 是 | Body | 无 | 读取到的当前修订号，0..2147483647 |
| draft | object | 是 | Body | 无 | 完整字段见模型表及Schema |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-result | 嵌套字段完整定义见下方模型表及contracts/admin-result.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/trade/admin/promotions/9/draft' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"requestNo":"ADMIN-edit-001","expectedRevision":1,"draft":{"name":"指定商品满25减1元","type":"FULL_REDUCTION","scope":{"kind":"SKU_IDS","ids":["101","102"]},"audience":"ALL_AUTHENTICATED","couponPolicy":"ALLOW_ONE","priority":10,"startsAtEpochMs":"1792886400000","endsAtEpochMs":"1793491200000","globalUseLimit":"10000","memberUseLimit":"2","rule":{"tiers":[{"thresholdFen":"2500","discountFen":"100"}]}}}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "id": "9",
    "revision": 2,
    "state": "PUBLISHED",
    "activeVersion": 1
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 3. 发布不可变版本

接口地址：`/trade/admin/promotions/{id}/publish`

请求方式：`POST`

权限：`mall:promotion:publish`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| id | string | 是 | Path | 无 | 正整数活动ID |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 是 | Body | 无 | 当前修订号，0..2147483647 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-result | 嵌套字段完整定义见下方模型表及contracts/admin-result.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/trade/admin/promotions/9/publish' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"requestNo":"ADMIN-publish-001","expectedRevision":1}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "id": "9",
    "revision": 2,
    "state": "PUBLISHED",
    "activeVersion": 1
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 4. 暂停、恢复或归档

接口地址：`/trade/admin/promotions/{id}/state`

请求方式：`PUT`

权限：`mall:promotion:state`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| id | string | 是 | Path | 无 | 正整数活动ID |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 是 | Body | 无 | 当前修订号，0..2147483647 |
| action | string | 是 | Body | 无 | 状态操作 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-result | 嵌套字段完整定义见下方模型表及contracts/admin-result.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/trade/admin/promotions/9/state' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"requestNo":"ADMIN-pause-001","expectedRevision":2,"action":"PAUSE"}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "id": "9",
    "revision": 2,
    "state": "PUBLISHED",
    "activeVersion": 1
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 5. 读取活动详情

接口地址：`/trade/admin/promotions/{id}`

请求方式：`GET`

权限：`mall:promotion:query`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| id | string | 是 | Path | 无 | 正整数活动ID |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-detail | 嵌套字段完整定义见下方模型表及contracts/admin-detail.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/trade/admin/promotions/9' --header 'Authorization: Bearer <TOKEN>'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "id": "9",
    "revision": 2,
    "state": "PUBLISHED",
    "activeVersion": 1,
    "code": "DESIGN_FULL_REDUCTION",
    "draft": null,
    "activeRule": {
      "name": "指定商品满25减1元",
      "type": "FULL_REDUCTION",
      "scope": {
        "kind": "SKU_IDS",
        "ids": [
          "101",
          "102"
        ]
      },
      "audience": "ALL_AUTHENTICATED",
      "couponPolicy": "ALLOW_ONE",
      "priority": 10,
      "startsAtEpochMs": "1792886400000",
      "endsAtEpochMs": "1793491200000",
      "globalUseLimit": "10000",
      "memberUseLimit": "2",
      "rule": {
        "tiers": [
          {
            "thresholdFen": "2500",
            "discountFen": "100"
          }
        ]
      }
    },
    "timePhase": "RUNNING"
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 6. 活动分页

接口地址：`/trade/admin/promotions`

请求方式：`GET`

权限：`mall:promotion:list`；权限定义脚本已提供，未执行或授予角色。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer管理员令牌，会员令牌不可用 |
| pageNum | integer | 否 | Query | 1 | 1..1000000 |
| pageSize | integer | 否 | Query | 20 | 1..100 |
| state | string | 否 | Query | 无 | DRAFT/PUBLISHED/PAUSED/ARCHIVED |
| code | string | 否 | Query | 无 | 精确活动编码，最多64字符 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | admin-page | 嵌套字段完整定义见下方模型表及contracts/admin-page.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/trade/admin/promotions?pageNum=1&pageSize=20&state=PUBLISHED' --header 'Authorization: Bearer <TOKEN>'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": "9",
        "revision": 2,
        "state": "PUBLISHED",
        "activeVersion": 1
      }
    ],
    "total": "1"
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 7. 生成并缓存服务端报价

接口地址：`/trade/app/v2/checkout/quote`

请求方式：`POST`

权限：`独立会员会话`；管理权限码尚未创建/授权。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，按入口隔离 |
| productIds | array<string> | 是 | Body | 无 | 完整字段见模型表及Schema |
| couponId | string/null | 是 | Body | 无 | 完整字段见模型表及Schema |
| promotionMode | string | 是 | Body | 无 | BEST在指定券约束下择优；NONE不参与订单活动 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | quote-response | 嵌套字段完整定义见下方模型表及contracts/quote-response.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/trade/app/v2/checkout/quote' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"productIds":["101","102"],"couponId":"5","promotionMode":"BEST"}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "settlementId": "a8b6e08e-54e8-4da0-81cf-1070171f34af",
    "pricedAtEpochMs": "1792890000000",
    "expiresAtEpochMs": "1792890900000",
    "promotionMode": "BEST",
    "pricingProtocol": "PROMOTION_V2",
    "algorithmVersion": "promo-1",
    "originalFen": "3003",
    "campaignDiscountFen": "100",
    "couponDiscountFen": "580",
    "totalDiscountFen": "680",
    "payableFen": "2323",
    "selectedCampaign": {
      "id": "9",
      "version": 1,
      "name": "指定商品满25减1元",
      "type": "FULL_REDUCTION",
      "ruleSummary": "指定商品满25.00元减1.00元，可叠加一张指定券"
    },
    "selectedCoupon": {
      "id": "5",
      "templateId": "3",
      "name": "80%支付券",
      "ruleSummary": "活动后满28.00元，支付原价80%"
    },
    "lines": [
      {
        "productId": "101",
        "spuId": "11",
        "name": "商品A",
        "unitPriceFen": "1001",
        "quantity": 1,
        "grossFen": "1001",
        "campaignDiscountFen": "33",
        "couponDiscountFen": "193",
        "payableFen": "775",
        "unitBasePayableFen": "775",
        "extraFenUnitCount": 0
      },
      {
        "productId": "102",
        "spuId": "12",
        "name": "商品B",
        "unitPriceFen": "2002",
        "quantity": 1,
        "grossFen": "2002",
        "campaignDiscountFen": "67",
        "couponDiscountFen": "387",
        "payableFen": "1548",
        "unitBasePayableFen": "1548",
        "extraFenUnitCount": 0
      }
    ],
    "submittable": true
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 8. 仅由缓存生成订单

接口地址：`/trade/app/v2/checkout`

请求方式：`POST`

权限：`独立会员会话`；管理权限码尚未创建/授权。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，按入口隔离 |
| settlementId | string | 是 | Body | 无 | 服务端结算UUID |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | submit-response | 嵌套字段完整定义见下方模型表及contracts/submit-response.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/trade/app/v2/checkout' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"settlementId":"a8b6e08e-54e8-4da0-81cf-1070171f34af","requestNo":"MINI-submit-001"}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "pricingProtocol": "PROMOTION_V2",
    "settlementId": "a8b6e08e-54e8-4da0-81cf-1070171f34af",
    "requestNo": "MINI-submit-001",
    "order": {
      "orderNo": "TRDESIGN001",
      "status": "RESERVING",
      "originalFen": "3003",
      "totalDiscountFen": "680",
      "payableFen": "2323"
    }
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 9. 原请求恢复，不依赖Redis

接口地址：`/trade/app/v2/checkout/requests/{requestNo}`

请求方式：`GET`

权限：`独立会员会话`；管理权限码尚未创建/授权。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，按入口隔离 |
| requestNo | string | 是 | Path | 无 | 原请求幂等编号，不是会员ID |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | submit-response | 嵌套字段完整定义见下方模型表及contracts/submit-response.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/trade/app/v2/checkout/requests/MINI-submit-001' --header 'Authorization: Bearer <TOKEN>'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "pricingProtocol": "PROMOTION_V2",
    "settlementId": "a8b6e08e-54e8-4da0-81cf-1070171f34af",
    "requestNo": "MINI-submit-001",
    "order": {
      "orderNo": "TRDESIGN001",
      "status": "RESERVING",
      "originalFen": "3003",
      "totalDiscountFen": "680",
      "payableFen": "2323"
    }
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 10. 持久终止或返回原订单

接口地址：`/trade/app/v2/checkout/abandon`

请求方式：`POST`

权限：`独立会员会话`；管理权限码尚未创建/授权。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，按入口隔离 |
| settlementId | string | 是 | Body | 无 | 服务端结算UUID |
| requestNo | string | 是 | Body | 无 | 幂等请求编号，重试不可改变 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | abandon-response | 嵌套字段完整定义见下方模型表及contracts/abandon-response.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/trade/app/v2/checkout/abandon' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"settlementId":"a8b6e08e-54e8-4da0-81cf-1070171f34af","requestNo":"MINI-submit-001"}'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "pricingProtocol": "PROMOTION_V2",
    "settlementId": "a8b6e08e-54e8-4da0-81cf-1070171f34af",
    "requestNo": "MINI-submit-001",
    "abandoned": true
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

### 11. 读取本人订单持久优惠快照

接口地址：`/trade/app/v2/orders/{orderNo}/pricing`

请求方式：`GET`

权限：`独立会员会话`；管理权限码尚未创建/授权。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，按入口隔离 |
| orderNo | string | 是 | Path | 无 | 本人订单号 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | order-pricing-response | 嵌套字段完整定义见下方模型表及contracts/order-pricing-response.schema.json |
| msg | string/缺失 | 错误时安全提示；成功通常省略 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/trade/app/v2/orders/TRDESIGN001/pricing' --header 'Authorization: Bearer <TOKEN>'
```

成功响应示例（构造，未实测）：

```json
{
  "code": 200,
  "data": {
    "orderNo": "TRDESIGN001",
    "acceptedAtEpochMs": "1792890010000",
    "pricingProtocol": "PROMOTION_V2",
    "algorithmVersion": "promo-1",
    "originalFen": "3003",
    "campaignDiscountFen": "100",
    "couponDiscountFen": "580",
    "totalDiscountFen": "680",
    "payableFen": "2323",
    "selectedCampaign": {
      "id": "9",
      "version": 1,
      "name": "指定商品满25减1元",
      "type": "FULL_REDUCTION",
      "ruleSummary": "指定商品满25.00元减1.00元，可叠加一张指定券"
    },
    "selectedCoupon": {
      "id": "5",
      "templateId": "3",
      "name": "80%支付券",
      "ruleSummary": "活动后满28.00元，支付原价80%"
    },
    "lines": [
      {
        "productId": "101",
        "spuId": "11",
        "name": "商品A",
        "unitPriceFen": "1001",
        "quantity": 1,
        "grossFen": "1001",
        "campaignDiscountFen": "33",
        "couponDiscountFen": "193",
        "payableFen": "775",
        "unitBasePayableFen": "775",
        "extraFenUnitCount": 0
      },
      {
        "productId": "102",
        "spuId": "12",
        "name": "商品B",
        "unitPriceFen": "2002",
        "quantity": 1,
        "grossFen": "2002",
        "campaignDiscountFen": "67",
        "couponDiscountFen": "387",
        "payableFen": "1548",
        "unitBasePayableFen": "1548",
        "extraFenUnitCount": 0
      }
    ]
  }
}
```

适用错误见错误表；权限与对象归属检查先于返回任何原请求/订单详情。

## 完整模型字段

以下递归字段表列出所有对外字段，规则联合分支分别展开。Schema用于精确类型/枚举/边界，跨字段守恒/门槛/时间关系仍需领域验证。金额字符串、nullable以及模型中的空列表按表说明处理；行数组按SKU数值ID排序。

### activity-draft

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| name | string | 活动名称 |
| type | string | 活动类型 |
| scope | object | 必填 |
| scope.kind | string | 商品范围 |
| scope.ids | array<string> | 必填 |
| audience | 固定值 ALL_AUTHENTICATED | 首期仅有效登录会员 |
| couponPolicy | string | 是否允许一张指定券 |
| priority | integer | 同价且资源数相同时优先级越大越优先 |
| startsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| endsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| globalUseLimit | string/null | 总使用次数上限，null表示无限次数 |
| memberUseLimit | string/null | 每会员使用次数上限，null表示无限次数 |
| rule | object（规则判别联合） | 必填 |
| rule[分支1].tiers | array<object> | 必填 |
| rule[分支1].tiers[].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| rule[分支1].tiers[].discountFen | string | 非负整数分，0..Long.MAX_VALUE |
| rule[分支2].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| rule[分支2].payRateBps | integer | 支付比例，8000=支付80%；优惠额向下取分 |
| rule[分支2].maxDiscountFen | string/null | 必填 |

### activity-create

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| requestNo | string | 幂等请求编号，重试不可改变 |
| code | string | 活动唯一编码，不可修改 |
| draft | object | 必填 |
| draft.name | string | 活动名称 |
| draft.type | string | 活动类型 |
| draft.scope | object | 必填 |
| draft.scope.kind | string | 商品范围 |
| draft.scope.ids | array<string> | 必填 |
| draft.audience | 固定值 ALL_AUTHENTICATED | 首期仅有效登录会员 |
| draft.couponPolicy | string | 是否允许一张指定券 |
| draft.priority | integer | 同价且资源数相同时优先级越大越优先 |
| draft.startsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.endsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.globalUseLimit | string/null | 总使用次数上限，null表示无限次数 |
| draft.memberUseLimit | string/null | 每会员使用次数上限，null表示无限次数 |
| draft.rule | object（规则判别联合） | 必填 |
| draft.rule[分支1].tiers | array<object> | 必填 |
| draft.rule[分支1].tiers[].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支1].tiers[].discountFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].payRateBps | integer | 支付比例，8000=支付80%；优惠额向下取分 |
| draft.rule[分支2].maxDiscountFen | string/null | 必填 |

### activity-update

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| requestNo | string | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 读取到的当前修订号 |
| draft | object | 必填 |
| draft.name | string | 活动名称 |
| draft.type | string | 活动类型 |
| draft.scope | object | 必填 |
| draft.scope.kind | string | 商品范围 |
| draft.scope.ids | array<string> | 必填 |
| draft.audience | 固定值 ALL_AUTHENTICATED | 首期仅有效登录会员 |
| draft.couponPolicy | string | 是否允许一张指定券 |
| draft.priority | integer | 同价且资源数相同时优先级越大越优先 |
| draft.startsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.endsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.globalUseLimit | string/null | 总使用次数上限，null表示无限次数 |
| draft.memberUseLimit | string/null | 每会员使用次数上限，null表示无限次数 |
| draft.rule | object（规则判别联合） | 必填 |
| draft.rule[分支1].tiers | array<object> | 必填 |
| draft.rule[分支1].tiers[].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支1].tiers[].discountFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].payRateBps | integer | 支付比例，8000=支付80%；优惠额向下取分 |
| draft.rule[分支2].maxDiscountFen | string/null | 必填 |

### activity-publish

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| requestNo | string | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 当前修订号 |

### activity-state

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| requestNo | string | 幂等请求编号，重试不可改变 |
| expectedRevision | integer | 当前修订号 |
| action | string | 状态操作 |

### quote-request

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| productIds | array<string> | 必填 |
| couponId | string/null | 必填 |
| promotionMode | string | BEST在指定券约束下择优；NONE不参与订单活动 |

### quote-response

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| settlementId | string | 服务端结算UUID |
| pricedAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| expiresAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| promotionMode | string | 用户确认的活动选择 |
| pricingProtocol | 固定值 PROMOTION_V2 | 必填 |
| algorithmVersion | 固定值 promo-1 | 必填 |
| originalFen | string | 非负整数分，0..Long.MAX_VALUE |
| campaignDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| couponDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| totalDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| payableFen | string | 非负整数分，0..Long.MAX_VALUE |
| selectedCampaign | object/null | 可省略 |
| selectedCampaign.id | string | 正整数ID，以字符串传递 |
| selectedCampaign.version | integer | 不可变活动版本 |
| selectedCampaign.name | string | 版本化活动名 |
| selectedCampaign.type | string | 活动类型 |
| selectedCampaign.ruleSummary | string | 服务端生成的规则说明，纯文本 |
| selectedCoupon | object/null | 可省略 |
| selectedCoupon.id | string | 正整数ID，以字符串传递 |
| selectedCoupon.templateId | string | 正整数ID，以字符串传递 |
| selectedCoupon.name | string | 优惠券名称 |
| selectedCoupon.ruleSummary | string | 规则说明，纯文本 |
| lines | array<object> | 必填 |
| lines[].productId | string | 正整数ID，以字符串传递 |
| lines[].spuId | string | 正整数ID，以字符串传递 |
| lines[].name | string | 商品展示名 |
| lines[].unitPriceFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].quantity | integer | 件数 |
| lines[].grossFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].campaignDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].couponDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].payableFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].unitBasePayableFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].extraFenUnitCount | integer | 单位实付多1分的件数，须小于quantity |
| submittable | boolean | 必填 |
| blockReason | string/null | 可省略 |

### submit-request

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| settlementId | string | 服务端结算UUID |
| requestNo | string | 幂等请求编号，重试不可改变 |

### submit-response

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| pricingProtocol | 固定值 PROMOTION_V2 | 必填 |
| settlementId | string | 服务端结算UUID |
| requestNo | string | 幂等请求编号，重试不可改变 |
| order | object | 必填 |
| order.orderNo | string | 订单编号 |
| order.status | string | 当前订单状态 |
| order.originalFen | string | 非负整数分，0..Long.MAX_VALUE |
| order.totalDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| order.payableFen | string | 非负整数分，0..Long.MAX_VALUE |

### abandon-response

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| [分支1].pricingProtocol | 固定值 PROMOTION_V2 | 必填 |
| [分支1].settlementId | string | 服务端结算UUID |
| [分支1].requestNo | string | 幂等请求编号，重试不可改变 |
| [分支1].abandoned | 固定值 True | 必填 |
| [分支2].pricingProtocol | 固定值 PROMOTION_V2 | 必填 |
| [分支2].settlementId | string | 服务端结算UUID |
| [分支2].requestNo | string | 幂等请求编号，重试不可改变 |
| [分支2].abandoned | 固定值 False | 必填 |
| [分支2].order | object | 必填 |
| [分支2].order.orderNo | string | 订单编号 |
| [分支2].order.status | string | 当前订单状态 |
| [分支2].order.originalFen | string | 非负整数分，0..Long.MAX_VALUE |
| [分支2].order.totalDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| [分支2].order.payableFen | string | 非负整数分，0..Long.MAX_VALUE |

### order-pricing-response

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| orderNo | string | 订单编号 |
| acceptedAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| pricingProtocol | 固定值 PROMOTION_V2 | 必填 |
| algorithmVersion | 固定值 promo-1 | 必填 |
| originalFen | string | 非负整数分，0..Long.MAX_VALUE |
| campaignDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| couponDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| totalDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| payableFen | string | 非负整数分，0..Long.MAX_VALUE |
| selectedCampaign | object/null | 可省略 |
| selectedCampaign.id | string | 正整数ID，以字符串传递 |
| selectedCampaign.version | integer | 不可变活动版本 |
| selectedCampaign.name | string | 版本化活动名 |
| selectedCampaign.type | string | 活动类型 |
| selectedCampaign.ruleSummary | string | 服务端生成的规则说明，纯文本 |
| selectedCoupon | object/null | 可省略 |
| selectedCoupon.id | string | 正整数ID，以字符串传递 |
| selectedCoupon.templateId | string | 正整数ID，以字符串传递 |
| selectedCoupon.name | string | 优惠券名称 |
| selectedCoupon.ruleSummary | string | 规则说明，纯文本 |
| lines | array<object> | 必填 |
| lines[].productId | string | 正整数ID，以字符串传递 |
| lines[].spuId | string | 正整数ID，以字符串传递 |
| lines[].name | string | 商品展示名 |
| lines[].unitPriceFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].quantity | integer | 件数 |
| lines[].grossFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].campaignDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].couponDiscountFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].payableFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].unitBasePayableFen | string | 非负整数分，0..Long.MAX_VALUE |
| lines[].extraFenUnitCount | integer | 单位实付多1分的件数，须小于quantity |

### admin-result

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 正整数ID，以字符串传递 |
| revision | integer | 操作完成后的根修订号 |
| state | string | 持久状态；窗口阶段另算 |
| activeVersion | integer/null | 必填 |

### admin-detail

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 正整数ID，以字符串传递 |
| revision | integer | 操作完成后的根修订号 |
| state | string | 持久状态；窗口阶段另算 |
| activeVersion | integer/null | 必填 |
| code | string | 唯一活动编码 |
| draft | object/null | 必填 |
| draft.name | string | 活动名称 |
| draft.type | string | 活动类型 |
| draft.scope | object | 必填 |
| draft.scope.kind | string | 商品范围 |
| draft.scope.ids | array<string> | 必填 |
| draft.audience | 固定值 ALL_AUTHENTICATED | 首期仅有效登录会员 |
| draft.couponPolicy | string | 是否允许一张指定券 |
| draft.priority | integer | 同价且资源数相同时优先级越大越优先 |
| draft.startsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.endsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| draft.globalUseLimit | string/null | 总使用次数上限，null表示无限次数 |
| draft.memberUseLimit | string/null | 每会员使用次数上限，null表示无限次数 |
| draft.rule | object（规则判别联合） | 必填 |
| draft.rule[分支1].tiers | array<object> | 必填 |
| draft.rule[分支1].tiers[].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支1].tiers[].discountFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| draft.rule[分支2].payRateBps | integer | 支付比例，8000=支付80%；优惠额向下取分 |
| draft.rule[分支2].maxDiscountFen | string/null | 必填 |
| activeRule | object/null | 必填 |
| activeRule.name | string | 活动名称 |
| activeRule.type | string | 活动类型 |
| activeRule.scope | object | 必填 |
| activeRule.scope.kind | string | 商品范围 |
| activeRule.scope.ids | array<string> | 必填 |
| activeRule.audience | 固定值 ALL_AUTHENTICATED | 首期仅有效登录会员 |
| activeRule.couponPolicy | string | 是否允许一张指定券 |
| activeRule.priority | integer | 同价且资源数相同时优先级越大越优先 |
| activeRule.startsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| activeRule.endsAtEpochMs | string | UTC epoch毫秒，十进制字符串 |
| activeRule.globalUseLimit | string/null | 必填 |
| activeRule.memberUseLimit | string/null | 必填 |
| activeRule.rule | object（规则判别联合） | 必填 |
| activeRule.rule[分支1].tiers | array<object> | 必填 |
| activeRule.rule[分支1].tiers[].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| activeRule.rule[分支1].tiers[].discountFen | string | 非负整数分，0..Long.MAX_VALUE |
| activeRule.rule[分支2].thresholdFen | string | 非负整数分，0..Long.MAX_VALUE |
| activeRule.rule[分支2].payRateBps | integer | 支付比例，8000=支付80%；优惠额向下取分 |
| activeRule.rule[分支2].maxDiscountFen | string/null | 必填 |
| timePhase | string/null | 必填 |

### admin-page

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| records | array<object> | 必填 |
| records[].id | string | 正整数ID，以字符串传递 |
| records[].revision | integer | 操作完成后的根修订号 |
| records[].state | string | 持久状态；窗口阶段另算 |
| records[].activeVersion | integer/null | 必填 |
| total | string | 匹配活动总数，十进制非负整数字符串 |

### error-response

| 字段路径 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 与新协议HTTP错误码一致，旧协议不改 |
| msg | string | 安全提示，不含内部规则/SQL/他人身份 |
| data | object | 必填 |
| data.errorCode | string | 稳定业务错误码 |
| data.retryable | boolean | 必填 |
| data.action | string | 客户端下一步 |
| data.correlationId | string | 服务端关联号，无凭证/个人资料 |

## 错误与客户端动作

已实现后台错误表见[management.md](management.md#当前管理错误http与rcode一致)：401 ADMIN_LOGIN_REQUIRED、404 PROMOTION_NOT_FOUND、409状态/限额/编码冲突、500 PROMOTION_INTERNAL_ERROR及503后台REFRESH_ADMIN。下表中V2购买链路部分仍为拟定，不能视为已部署行为。

| HTTP/R.code | data.errorCode | retryable / action | 含义 |
| --- | --- | --- | --- |
| 400 | INVALID_PROMOTION_REQUEST / AMOUNT_OUT_OF_RANGE | false / FIX_INPUT | 参数、未知字段、玩法、范围或金额非法 |
| 401 | MEMBER_SESSION_INVALID | false / LOGIN | 当前独立会员会话无效，不能用管理员身份替代 |
| 403 | PROMOTION_PERMISSION_DENIED | false / FIX_INPUT | 管理员无对应权限 |
| 404 | ORDER_NOT_FOUND / REQUEST_NOT_FOUND | true仅未知原请求 / KEEP_INTENT_AND_RECOVER | 外人/不存在订单不区分；查询未中不代表取消 |
| 409 | PROMOTION_REVISION_CONFLICT / ADMIN_REQUEST_CONFLICT | false / REFRESH_ADMIN | 后台修订或幂等内容冲突 |
| 409 | REQUEST_CONFLICT / REQUEST_PROTOCOL_CONFLICT | false / KEEP_INTENT_AND_RECOVER | key已有不同内容/协议，不能更换key重试原未知购买 |
| 409 | PROMOTION_SNAPSHOT_STALE / PROMOTION_QUOTA_CHANGED / CART_VERSION_CONSUMED | false / ABANDON_THEN_REQUOTE | 优惠/次数/购物车条件变化，不静默换方案 |
| 409 | COUPON_NOT_ELIGIBLE / ZERO_PAY_UNSUPPORTED | false / FIX_INPUT（报价）或ABANDON_THEN_REQUOTE（已有提单意图） | 指定券不可用或当前不支持零元成单 |
| 409 | PRICING_SNAPSHOT_UNAVAILABLE | false / VIEW_LEGACY_ORDER | 本人历史订单缺少V2分摊，不能补假明细 |
| 410 | SETTLEMENT_EXPIRED / SETTLEMENT_ABANDONED | false / ABANDON_THEN_REQUOTE | 报价已过期/终止 |
| 500 | PRICING_INVARIANT_VIOLATION | false / KEEP_INTENT_AND_RECOVER | 账本/金额不变量损坏，保留原请求并人工核验，不能补造新单 |
| 503 | CATALOG_FACTS_UNAVAILABLE / PRICING_TEMPORARILY_UNAVAILABLE / PROMOTION_CANDIDATE_LIMIT / SETTLEMENT_TOO_LARGE | true / KEEP_INTENT_AND_RECOVER | 不回退客户端价格/无优惠；无提单意图的报价请求可安全稍后重新报价 |

参数拒绝、原请求已成单、未成单、权限错误都不允许返回内部SQL、完整规则JSON、他人会员ID或凭证。新服务必须将这些稳定错误码映射为专用ErrorRes；此表不是现有接口已经返回的事实。

错误构造示例：

```json
{
  "code": 409,
  "msg": "优惠条件已变化，请先终止原提单再重新结算",
  "data": {
    "errorCode": "PROMOTION_SNAPSHOT_STALE",
    "retryable": false,
    "action": "ABANDON_THEN_REQUOTE",
    "correlationId": "design-example-001"
  }
}
```

空分页构造示例：

```json
{"code":200,"data":{"records":[],"total":"0"}}
```

## 实施与兼容检查

- contracts是JSON Schema Draft2020-12的设计附件，自定义int64格式必须启用校验；它们没有被接入Spring、OpenAPI或运行时路由。
- ruleHash、原始候选、memberId、库存/规则内部事实留在受信快照，不接受客户端提交。严禁只校验Schema而跳过权限、数据库事实与金额守恒。
- 提单在所有必要行锁取得后，再取一次DB时间检查报价/窗口/券；资格按该核验时刻决定。mall读事实与trade提交之间不存在跨库原子锁，库存仍由Outbox预留结果决定；订单只兑现已核验并缓存的快照，不能宣称全链路原子读价。
- 新管理权限码/菜单、V2网关私有路径、RPC provider、迁移及生命周期worker须按design.md任务部署验证；本轮均未实施。停新单后仍须支持原请求恢复、终止和既有订单确认/释放。
- 需要重结算的已发起提单必须先确认终止原settlement并保留其历史，不把409/404/网络失败当作本地可以直接丢弃意图的许可。

未知errorCode默认保留原提单意图并报错，不能解释为无优惠、已取消或可换key重试。
