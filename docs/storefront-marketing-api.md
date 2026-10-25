# 小程序商品与优惠券营销接口

v7 · 2026-10-25 · 基础957c75a。依据本仓库实际控制器、Req/Res、SQL和事务实现；格式参考 code-software-engineer/references/api-documentation.md。下列JSON和curl均为构造示例，不代表已部署HTTP或供应商实测。

## 范围与权限

- 公开 GET `/mall/app/categories`、`/mall/app/products`、`/mall/app/products/{id}`；网关仅按精确方法/路径放行并清除伪造身份头，不放行管理接口或写操作。复用保留 `/mall` 前缀的既有网关路由。
- 商品须SPU/SKU上架、分类/父分类与品牌启用、价格为正；缺货商品仍可浏览，available仅代表存在有库存的在售规格。详情隐藏下架规格。不返回内部SKU编码和后台状态。分类上限1000，超限明确拒绝而非静默截断；分类筛选支持父分类及直接子分类。搜索参数绑定SQL，不由客户端传状态。
- 私有 `/trade/app/**` 使用独立会员Bearer会话；memberId只由服务端验证，不接受请求体、查询或伪造身份头决定用户。后台公开领取设置沿用现有管理员令牌及权限。无需新角色权限码，未自动授予角色。
- 优惠券现有模板默认不允许自行领取。后台须显式开放public-claim，并且模板启用、当前处在有效期、配额未满；每会员每模板最多一张。已领取重试返回原券，即使此后关闭公开领取或模板停用也不重复发放。关闭公开领取不影响已发券；停用模板会阻止新兑换，既有锁定订单按既有规则处理。
- 优惠券有效期左闭右开，使用交易数据库时钟。钱包返回持有券及完整规则、couponStatus和当前usable；AVAILABLE不等于可用，过期/未生效/停用均不可用。claimable/usable只代表查询时状态，结算仍复核金额、归属、规则和锁定状态。
- 增量加购quantity是本次增加量，不覆盖已有数量；总数不超过999和当次可用库存。requestNo在会员范围内幂等，相同请求返回首次数量快照；同编号不同商品或数量拒绝。购物车条目之后删除或被订单消费，原请求重放也不会重建。加购不预留库存，结算和下单再次核验。

## 返回与错误

使用现有R包装：code成功200，data为下列对象；全局Jackson省略null字段，不能把缺失的couponId误认为0。金额始终以元为单位的精确两位小数字符串返回；ID可能是JSON整数或字符串（大Long防精度丢失），客户端统一按字符串保存。日期时间yyyy-MM-dd HH:mm:ss，展示数据库时间，不在客户端自行改变有效期语义。

公开详情不存在/隐藏统一返回data=null；部署时null字段可能直接省略。库存和权益是查询快照。原加购回执的resultingQuantity不是当前购物车数量，应刷新购物车核验。普通业务异常沿用现有公共处理器，客户端同时检查HTTP和R.code；不得把HTTP200认作成功。私有控制器认证失败HTTP401+R.fail，参数校验失败HTTP400；网关沿用HTTP200+R.code401/503。构造认证错误示例：

```json
{"code":500,"msg":"会员登录已失效"}
```

## 接口

### 1. 公开启用分类

接口地址：`/mall/app/categories`
请求方式：`GET`
权限：`公开读取`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | java.util.List<StoreCategoryRes> | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/mall/app/categories'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "parentId": null,
      "name": "商品示例",
      "picUrl": "https://example.test/category.png"
    }
  ]
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 2. 公开在售商品分页，服务端价格区间

接口地址：`/mall/app/products`
请求方式：`GET`
权限：`公开读取`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| name | String | 否 | Query | 无 | 商品名称包含筛选 |
| categoryId | Long | 否 | Query | 无 | 分类或父分类ID |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 每页条数，默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | StorePageRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/mall/app/products?pageNum=1&pageSize=20'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "name": "商品示例",
        "categoryId": 2,
        "brandId": 3,
        "image": "https://example.test/product.png",
        "minPrice": "9.99",
        "maxPrice": "19.99",
        "available": true
      }
    ],
    "total": 1
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 3. 公开在售商品详情；不存在及隐藏均为null

接口地址：`/mall/app/products/{id}`
请求方式：`GET`
权限：`公开读取`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | 是 | Path | 无 | 正整数ID；加购productId为SKU，公开商品id为SPU |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | StoreDetailRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/mall/app/products/1'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "商品示例",
    "categoryId": 2,
    "brandId": 3,
    "description": "商品描述纯文本",
    "image": "https://example.test/product.png",
    "skus": [
      {
        "id": 1,
        "name": "商品示例",
        "price": "9.99",
        "stock": 5,
        "image": "https://example.test/product.png",
        "properties": {
          "颜色": "红",
          "尺寸": "M"
        }
      }
    ]
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 4. 已开启公开领取的模板ID；不代表当前可领取

接口地址：`/trade/admin/coupon-templates/public-claim`
请求方式：`GET`
权限：`trade:coupon-template:list`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | java.util.List<Long> | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/trade/admin/coupon-templates/public-claim' --header 'Authorization: Bearer <TOKEN>'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": [
    1
  ]
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 5. 开启或关闭会员自助领取；现有模板默认关闭

接口地址：`/trade/admin/coupon-templates/{id}/public-claim`
请求方式：`PUT`
权限：`trade:coupon-template:edit`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |
| id | integer/string | 是 | Path | 无 | 正整数ID；加购productId为SKU，公开商品id为SPU |
| enabled | Boolean | 是 | Body | 无 | 是否允许会员自行领取，默认关闭 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | Boolean | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request PUT 'https://<API_HOST>/trade/admin/coupon-templates/3/public-claim' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"enabled": true}'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": true
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 6. 本人可见的公开领券中心

接口地址：`/trade/app/coupon-center`
请求方式：`GET`
权限：`独立会员会话`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |
| pageNum | Integer | 否 | Query | 1 | 页码默认1 |
| pageSize | Integer | 否 | Query | 20 | 每页默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | PromotionPageRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/trade/app/coupon-center?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "template": {
          "id": 3,
          "code": "WELCOME",
          "name": "新客满减券",
          "kind": "FIXED",
          "threshold": "10.00",
          "fixedAmount": "2.00",
          "totalCount": 100,
          "issuedCount": 1,
          "validFrom": "2026-10-25 00:00:00",
          "validUntil": "2026-11-01 00:00:00",
          "enabled": true
        },
        "usable": false,
        "claimable": true
      }
    ],
    "total": 1
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 7. 本人优惠券及服务端当前可用状态

接口地址：`/trade/app/coupon-wallet`
请求方式：`GET`
权限：`独立会员会话`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |
| pageNum | Integer | 否 | Query | 1 | 页码默认1 |
| pageSize | Integer | 否 | Query | 20 | 每页默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | PromotionPageRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request GET 'https://<API_HOST>/trade/app/coupon-wallet?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "template": {
          "id": 3,
          "code": "WELCOME",
          "name": "新客满减券",
          "kind": "FIXED",
          "threshold": "10.00",
          "fixedAmount": "2.00",
          "totalCount": 100,
          "issuedCount": 1,
          "validFrom": "2026-10-25 00:00:00",
          "validUntil": "2026-11-01 00:00:00",
          "enabled": true
        },
        "couponId": 5,
        "couponStatus": "AVAILABLE",
        "usable": true,
        "claimable": false
      }
    ],
    "total": 1
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 8. 本人领取公开优惠券，每模板限一张，重复返回原券

接口地址：`/trade/app/coupon-center/{templateId}/claim`
请求方式：`POST`
权限：`独立会员会话`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |
| templateId | integer/string | 是 | Path | 无 | 正整数ID；加购productId为SKU，公开商品id为SPU |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | CouponRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request POST 'https://<API_HOST>/trade/app/coupon-center/3/claim' --header 'Authorization: Bearer <TOKEN>'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "id": 5,
    "templateId": 3,
    "memberId": 1,
    "status": "AVAILABLE"
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

### 9. 会员范围内幂等增量加购，返回首次结果快照

接口地址：`/trade/app/cart/{productId}/add`
请求方式：`POST`
权限：`独立会员会话`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 是 | Header | 无 | Bearer会员或管理员令牌，依上述权限 |
| productId | integer/string | 是 | Path | 无 | 正整数ID；加购productId为SKU，公开商品id为SPU |
| quantity | int | 是 | Body | 无 | 本次增加数量1至999 |
| requestNo | String | 是 | Body | 无 | 会员范围内幂等请求编号 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200 |
| data | CartAdditionRes | 全部嵌套字段见模型表 |
| msg | string/缺失 | 错误消息，成功通常省略 |

请求示例（构造）：

```bash
curl --request POST 'https://<API_HOST>/trade/app/cart/7/add' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"quantity": 2, "requestNo": "ADD-demo-001"}'
```

成功响应（构造）：

```json
{
  "code": 200,
  "data": {
    "requestNo": "ADD-demo-001",
    "productId": 1,
    "addedQuantity": 2,
    "resultingQuantity": 4
  }
}
```

适用错误：参数不合法；私有接口会话失效；领券未开放/过期/配额不足；加购库存不足/请求编号内容冲突。错误按上方通用规则检查；公开查询不把缺货表示为认证失败。

## 模型完整字段

金额字段BigDecimal在实际JSON中按字符串序列化，示例中的List<Long>公开设置ID允许数值或字符串。nullable字段可省略。

### CartAddReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| quantity | int | 本次增加数量1至999 |
| requestNo | String | 会员范围内幂等请求编号 |

### CartAdditionRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| requestNo | String | 原请求编号 |
| productId | Long | SKU编号 |
| addedQuantity | int | 本次增加数量 |
| resultingQuantity | int | 首次成功后的数量快照，不代表当前购物车 |

### CouponOfferRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| template | CouponTemplateRes | 优惠券规则 |
| couponId | Long | 本人持有券ID，未领取为null |
| couponStatus | String | 本人券状态，未领取为null |
| usable | boolean | 服务端核验当前可用，不保证结算时仍有效 |
| claimable | boolean | 当前可领取 |

### CouponPublicClaimReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| enabled | Boolean | 是否允许会员自行领取，默认关闭 |

### CouponRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 编号 |
| templateId | Long | 优惠券模板编号 |
| memberId | Long | 后台授权操作的目标会员编号，不是当前后台用户ID |
| status | String | 订单RESERVING/PENDING_PAYMENT/PAID/CLOSED，优惠券AVAILABLE/LOCKED/USED |
| orderNo | String | 订单号 |

### CouponTemplateRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 编号 |
| code | String | 模板编码 |
| name | String | 名称，不可售商品可省略 |
| kind | String | FIXED或PERCENT |
| threshold | java.math.BigDecimal | 商品金额门槛，元 |
| fixedAmount | java.math.BigDecimal | 满减金额，元 |
| discountPercent | Integer | 支付百分比，80表示8折 |
| discountLimit | java.math.BigDecimal | 折扣金额上限，元 |
| totalCount | int | 总发放数量 |
| issuedCount | int | 已发放数量 |
| validFrom | java.time.LocalDateTime | 生效时间，yyyy-MM-dd HH:mm:ss |
| validUntil | java.time.LocalDateTime | 过期时间，右开边界 |
| enabled | boolean | 是否开放新发放及兑换 |

### PromotionPageReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | Integer | 页码默认1 |
| pageSize | Integer | 每页默认20，最大100 |

### PromotionPageRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| records | java.util.List<CouponOfferRes> | 当前页 |
| total | long | 总数 |

### StoreCategoryRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | id |
| parentId | Long | parentId |
| name | String | name |
| picUrl | String | picUrl |

### StoreDetailRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | id |
| name | String | name |
| categoryId | Long | categoryId |
| brandId | Long | brandId |
| description | String | description |
| image | String | image |
| skus | java.util.List<StoreSkuRes> | skus |

### StorePageReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| name | String | 商品名称包含筛选 |
| categoryId | Long | 分类或父分类ID |
| pageNum | Integer | 页码，默认1 |
| pageSize | Integer | 每页条数，默认20，最大100 |

### StorePageRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| records | java.util.List<StoreProductRes> | records |
| total | long | total |

### StoreProductRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | id |
| name | String | name |
| categoryId | Long | categoryId |
| brandId | Long | brandId |
| image | String | image |
| minPrice | String | 最低售价（元，精确两位小数字符串） |
| maxPrice | String | 最高售价（元，精确两位小数字符串） |
| available | boolean | 是否存在有库存的在售规格，结算时再次核验 |

### StoreSkuRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | id |
| name | String | name |
| price | String | 当前售价（元，精确两位小数字符串） |
| stock | int | 当前可用库存，不表示库存预留 |
| image | String | image |
| properties | java.util.Map<String, String> | 规格属性名到值的映射 |

## 页面、迁移与验证

小程序启动页为pages/store/index，进入pages/product/index?id=<SPU>选择完整SKU规格组合后增量加购，进入pages/index/index核验购物车和服务端缓存结算。pages/coupons/index为领券中心，?mode=wallet显示本人钱包；会员和购物车均提供入口。图像仅渲染HTTPS，描述仅作为纯文本，金额不经浮点换算。服务端不返回的区划、活动或库存承诺不在前端编造。

小程序加购先在本机会员专属lemnt.cart-add.v1.<memberId>保存非个人信息意图，确认读回后才发请求；未知结果保留同一requestNo与载荷。存储失败、会员切换、回执错配不丢弃意图；原请求重试忽略新输入。已确认后只删除核对过的同一记录，清理失败仍可重放原请求。新加购在存在未确认结算时拒绝，原加购回执重试仍允许。不可删除服务端幂等账本来解除本机未知结果。

先审阅并执行trade/sql/V8__storefront_marketing.sql（已有V1至V7之后），增加公开领取设置和加购请求账本。脚本未在运行库执行且不可重复执行。现有模板不填公开设置，默认为关闭。新端点和网关规则应一起部署；旧管理接口保留。退回旧服务前停止新客户端写入并保留加购/结算记录，未知请求需核验，不提供删账本的破坏性回滚。小程序配置仍memberApiReady=false，真实环境核验后再由部署人员启用。

本轮仅补齐商品浏览/加购和优惠券营销链路。秒杀、拼团、砍价、优惠叠加/相对有效期/退款返券、后续SKU新增删除、属性字典、运费模板和评价仍未迁移完成。会员等级/积分尚未接入订单计价；真实支付、微信/短信及MySQL/Redis/Nacos/Dubbo/浏览器/原生设备验收不因本轮测试而变成已完成。

验证证据在work-items/yudao-member-mall-trade-migration/evidence/storefront/：实际SQL/H2、事务回滚/并发、HTTP契约/网关边界和前端模拟行为测试。静态WXML检查不是原生编译，PR保持Draft，不宣称生产可用。
