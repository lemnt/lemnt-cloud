# Mall catalog API
Version v1 · 2026-10-25 · source checked, examples constructed (not deployed HTTP evidence).
Input: catalog design v2; Req/Res declarations; actual `JacksonConfig`/`BigNumberSerializer`. Local MVC validation and actual serialization are tested separately.

Amounts are decimal yuan. Application Jackson serializes BigDecimal as strings, and emits Long values outside the JavaScript safe range as strings. Null properties are omitted. UI normalizes all IDs to strings and retains price strings for display. Edit forms limit prices to 99,999,999,999.99 yuan to avoid numeric editing precision loss; larger prices remain displayable and need a separate precise administrative workflow (not implemented by this UI).

Backend service paths retain `/mall`; see `config/mall-gateway-route.yaml`. Permission enforcement uses existing admin security; no member checkout endpoints or role grants are introduced.

## Shared response schemas
`R` fields: `code` integer; `msg` nullable/omitted text; `data` object/boolean/array for non-paged endpoints; `rows` array and `total` integer for pagination. Unused/null fields are omitted by application serialization. `code=200` denotes application success. A Boolean false means no row was changed; clients must not announce success.

### Category
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| parentId | integer/string | 父分类编号，根为null |
| name | string | 名称 |
| picUrl | string | 图片URL |
| sort | integer | 显示排序 |
| status | string | 状态 |

### Brand
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| name | string | 名称 |
| picUrl | string | 图片URL |
| description | string | 描述 |
| sort | integer | 显示排序 |
| status | string | 状态 |

### Sku
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| spuId | integer/string | 所属SPU编号，存量独立商品为null |
| code | string | 编码 |
| name | string | 名称 |
| price | decimal string on response; number/string on request | 价格，元 |
| stock | integer | 可用库存 |
| image | string | 图片URL |
| properties | object | SKU规格映射 |
| properties.<name> | string | SKU specification value |
| status | string | 状态 |

### Spu
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| code | string | 编码 |
| name | string | 名称 |
| categoryId | integer/string | 分类编号 |
| brandId | integer/string | 品牌编号 |
| description | string | 描述 |
| image | string | 图片URL |
| status | string | 状态 |
| skus | array | SKU列表，分页列表时为空，详情包含完整SKU |
| skus[] | Sku | Full nested Sku schema above; always empty in paged rows, populated in detail/create responses |

### StockAdjustment
| Field | Type | Meaning |
| --- | --- | --- |
| requestNo | string | 库存请求编号 |
| productId | integer/string | SKU编号 |
| delta | integer | 库存增减量 |
| reason | string | 变更原因 |
| resultingStock | integer | 该请求执行后的库存快照 |

## 1. Category list
Address: `/mall/categories`. Method: `GET`. Permission: `mall:category:list`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| none | — | — | — | — | No parameters |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data[] | Category | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "服装",
      "sort": 0,
      "status": "ENABLED"
    }
  ]
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/categories' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 2. Category query
Address: `/mall/categories/{id}`. Method: `GET`. Permission: `mall:category:query`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Category | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "服装",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/categories/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 3. Category add
Address: `/mall/categories`. Method: `POST`. Permission: `mall:category:add`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| parentId | integer/string | no | body | none | 父分类编号，根分类为null; @Positive |
| name | string | yes | body | none | 分类名称; @NotBlank @Size(max=100) |
| picUrl | string | no | body | none | 分类图片URL; @Size(max=500) |
| sort | integer | yes | body | none | 显示排序; @NotNull @Min(0) |
| status | string | yes | body | none | ENABLED或DISABLED; @NotBlank @Pattern(regexp="ENABLED or DISABLED") |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Category | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "服装",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X POST 'https://<API_HOST>/mall/categories' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"parentId":null,"name":"服装","sort":0,"status":"ENABLED"}'
```

## 4. Category edit
Address: `/mall/categories/{id}`. Method: `PUT`. Permission: `mall:category:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| parentId | integer/string | no | body | none | 父分类编号，根分类为null; @Positive |
| name | string | yes | body | none | 分类名称; @NotBlank @Size(max=100) |
| picUrl | string | no | body | none | 分类图片URL; @Size(max=500) |
| sort | integer | yes | body | none | 显示排序; @NotNull @Min(0) |
| status | string | yes | body | none | ENABLED或DISABLED; @NotBlank @Pattern(regexp="ENABLED or DISABLED") |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Category | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "服装",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X PUT 'https://<API_HOST>/mall/categories/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"parentId":null,"name":"服装","sort":0,"status":"ENABLED"}'
```

## 5. Category remove
Address: `/mall/categories/{id}`. Method: `DELETE`. Permission: `mall:category:remove`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Boolean | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Request example (host/token placeholders):
```bash
curl -X DELETE 'https://<API_HOST>/mall/categories/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 6. Brand list
Address: `/mall/brands`. Method: `GET`. Permission: `mall:brand:list`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| none | — | — | — | — | No parameters |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data[] | Brand | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": [
    {
      "id": 2,
      "name": "示例品牌",
      "sort": 0,
      "status": "ENABLED"
    }
  ]
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/brands' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 7. Brand query
Address: `/mall/brands/{id}`. Method: `GET`. Permission: `mall:brand:query`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Brand | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "name": "示例品牌",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/brands/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 8. Brand add
Address: `/mall/brands`. Method: `POST`. Permission: `mall:brand:add`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| name | string | yes | body | none | 品牌名称; @NotBlank @Size(max=100) |
| picUrl | string | no | body | none | 品牌图片URL; @Size(max=500) |
| description | string | no | body | none | 品牌描述; @Size(max=2000) |
| sort | integer | yes | body | none | 显示排序; @NotNull @Min(0) |
| status | string | yes | body | none | ENABLED或DISABLED; @NotBlank @Pattern(regexp="ENABLED or DISABLED") |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Brand | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "name": "示例品牌",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X POST 'https://<API_HOST>/mall/brands' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"name":"品牌","sort":0,"status":"ENABLED"}'
```

## 9. Brand edit
Address: `/mall/brands/{id}`. Method: `PUT`. Permission: `mall:brand:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| name | string | yes | body | none | 品牌名称; @NotBlank @Size(max=100) |
| picUrl | string | no | body | none | 品牌图片URL; @Size(max=500) |
| description | string | no | body | none | 品牌描述; @Size(max=2000) |
| sort | integer | yes | body | none | 显示排序; @NotNull @Min(0) |
| status | string | yes | body | none | ENABLED或DISABLED; @NotBlank @Pattern(regexp="ENABLED or DISABLED") |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Brand | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "name": "示例品牌",
    "sort": 0,
    "status": "ENABLED"
  }
}
```

Request example (host/token placeholders):
```bash
curl -X PUT 'https://<API_HOST>/mall/brands/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"name":"品牌","sort":0,"status":"ENABLED"}'
```

## 10. Brand remove
Address: `/mall/brands/{id}`. Method: `DELETE`. Permission: `mall:brand:remove`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Boolean | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Request example (host/token placeholders):
```bash
curl -X DELETE 'https://<API_HOST>/mall/brands/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 11. SPU page
Address: `/mall/spus`. Method: `GET`. Permission: `mall:spu:list`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| name | string | no | query | none | 商品名称包含匹配; @Size(max=200) |
| status | string | no | query | none | ON_SHELF或OFF_SHELF，可不传;  |
| pageNum | integer | no | query | 1 | 页码，默认1; @Min(1) @Max(1000000) |
| pageSize | integer | no | query | 10 | 每页条数，默认10，最大100; @Min(1) @Max(100) |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| rows[] | Spu | Shared schema, skus=[] |
| total | integer/string | Matching SPU count; pagination is 1..100 rows, pageNum 1..1000000 |

Constructed response:
```json
{
  "code": 200,
  "rows": [
    {
      "id": 3,
      "code": "SPU-001",
      "name": "示例商品",
      "categoryId": 1,
      "brandId": 2,
      "status": "OFF_SHELF",
      "skus": []
    }
  ],
  "total": 1
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/spus' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 12. SPU detail
Address: `/mall/spus/{id}`. Method: `GET`. Permission: `mall:spu:query`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Spu | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 3,
    "code": "SPU-001",
    "name": "示例商品",
    "categoryId": 1,
    "brandId": 2,
    "status": "OFF_SHELF",
    "skus": [
      {
        "id": 5,
        "spuId": 3,
        "code": "SKU-RED-M",
        "name": "红色 M",
        "price": "99.00",
        "stock": 5,
        "properties": {
          "颜色": "红色",
          "尺寸": "M"
        },
        "status": "ON_SHELF"
      }
    ]
  }
}
```

Request example (host/token placeholders):
```bash
curl -X GET 'https://<API_HOST>/mall/spus/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 13. SPU create
Address: `/mall/spus`. Method: `POST`. Permission: `mall:spu:add`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| code | string | yes | body | none | SPU编码; @NotBlank @Size(max=64) |
| name | string | yes | body | none | 商品名称; @NotBlank @Size(max=200) |
| categoryId | integer/string | yes | body | none | 分类编号; @NotNull @Positive |
| brandId | integer/string | yes | body | none | 品牌编号; @NotNull @Positive |
| description | string | no | body | none | 商品描述; @Size(max=65535) |
| image | string | no | body | none | 主图URL; @Size(max=500) |
| skus | array | yes | body | none | SKU列表，1至100项; @NotEmpty @Size(max=100) |
| skus[].code | string | yes | body | none | SKU编码 |
| skus[].name | string | yes | body | none | SKU名称 |
| skus[].price | decimal string on response; number/string on request | yes | body | none | 销售价，元，最多两位小数 |
| skus[].stock | integer | yes | body | none | 可用库存 |
| skus[].image | string | no | body | none | SKU图片URL |
| skus[].properties | object | no | body | none | 规格名到规格值的映射，最多10项 |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Spu | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 3,
    "code": "SPU-001",
    "name": "示例商品",
    "categoryId": 1,
    "brandId": 2,
    "status": "OFF_SHELF",
    "skus": [
      {
        "id": 5,
        "spuId": 3,
        "code": "SKU-RED-M",
        "name": "红色 M",
        "price": "99.00",
        "stock": 5,
        "properties": {
          "颜色": "红色",
          "尺寸": "M"
        },
        "status": "ON_SHELF"
      }
    ]
  }
}
```

Request example (host/token placeholders):
```bash
curl -X POST 'https://<API_HOST>/mall/spus' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"code":"SPU-001","name":"商品","categoryId":"1","brandId":"2","skus":[{"code":"SKU-RED-M","name":"红色 M","price":99,"stock":5,"properties":{"颜色":"红色","尺寸":"M"}}]}'
```

## 14. SPU metadata
Address: `/mall/spus/{id}`. Method: `PUT`. Permission: `mall:spu:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| name | string | yes | body | none | 商品名称; @NotBlank @Size(max=200) |
| categoryId | integer/string | yes | body | none | 分类编号; @NotNull @Positive |
| brandId | integer/string | yes | body | none | 品牌编号; @NotNull @Positive |
| description | string | no | body | none | 商品描述; @Size(max=65535) |
| image | string | no | body | none | 主图URL; @Size(max=500) |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Boolean | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Request example (host/token placeholders):
```bash
curl -X PUT 'https://<API_HOST>/mall/spus/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"name":"商品新名称","categoryId":"1","brandId":"2"}'
```

## 15. SPU shelf
Address: `/mall/spus/{id}/shelf`. Method: `PUT`. Permission: `mall:spu:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| onShelf | boolean | yes | body | none | 是否上架; @NotNull |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Boolean | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Request example (host/token placeholders):
```bash
curl -X PUT 'https://<API_HOST>/mall/spus/3/shelf' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"onShelf":true}'
```

## 16. SKU metadata
Address: `/mall/skus/{id}`. Method: `PUT`. Permission: `mall:sku:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| name | string | yes | body | none | SKU名称; @NotBlank @Size(max=200) |
| price | decimal string on response; number/string on request | yes | body | none | 销售价，元; @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) |
| image | string | no | body | none | SKU图片URL; @Size(max=500) |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | Boolean | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Request example (host/token placeholders):
```bash
curl -X PUT 'https://<API_HOST>/mall/skus/3' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"name":"红色 M","price":99}'
```

## 17. SKU stock
Address: `/mall/skus/{id}/stock`. Method: `POST`. Permission: `mall:sku:stock`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | integer/string | yes | path | none | Positive entity ID; stock route uses existing SKU/product ID |
| requestNo | string | yes | body | none | 库存幂等请求编号; @NotBlank @Size(max=64) |
| delta | integer | yes | body | none | 库存增减量，非0，不能覆盖绝对值; @NotNull |
| reason | string | yes | body | none | 变更原因; @NotBlank @Size(max=200) |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on success |
| msg | string, omitted if null | Existing R result message |
| data | StockAdjustment | Shared schema above (including nested skus[] and properties entries where present) |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "requestNo": "STOCK-001",
    "productId": 5,
    "delta": 2,
    "reason": "补货",
    "resultingStock": 7
  }
}
```

Request example (host/token placeholders):
```bash
curl -X POST 'https://<API_HOST>/mall/skus/3/stock' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"requestNo":"STOCK-001","delta":2,"reason":"补货"}'
```

## Error and state semantics
- Parameter binding/Bean Validation rejects missing required fields, invalid decimal precision, nested SKU violations and paging limits before the application write. MVC tests verify 400 for these cases. Domain failures and persistence errors follow the existing global handler; deployed gateway/security response shapes have not been exercised.
- Missing detail returns existing R.fail("record missing") behavior; delete/update/shelf of a missing row returns false. Arrays are genuinely empty only when the backend supplies an empty array. Invalid/missing response arrays are rejected by the catalog UI decoder.
- Referenced category/brand deletion fails; parent category cannot be changed; categories are limited to two levels. List operations reject counts above 1000 instead of truncating silently.
- SPU create accepts 1..100 SKUs and at most 10 properties per SKU; property-name sets must match and variant combinations/code must be unique. Creation is one local transaction. New SPU starts OFF_SHELF. Codes and variant identity are immutable in the present update API.
- Publishing validates enabled category, ancestors and brand. Snapshot lookup and reservation SQL independently require SPU/category/brand availability. Existing legacy standalone products retain their original behavior. Confirm/release works for previously reserved stock even if taxonomy/SPU is now disabled.
- Stock adjustments require nonzero delta, positive SKU ID, nonblank requestNo <=64 characters, reason <=200. Same requestNo/product/delta/reason returns the original resultingStock snapshot; changed payload rejects. Concurrent duplicate insertion can return a retryable conflict; retry with identical requestNo and payload.
- Inventory adjustment checks nonnegative stock, signed INT maximum and capacity for outstanding RESERVED releases. Ledger and inventory commit together. On unknown network outcome, do not invent a new requestNo: retry the original or inspect the ledger.

## Setup and rollout
1. Apply mall `V2__mall_catalog.sql` after existing mall V1 in an isolated MySQL first. It adds tables/nullable columns/FKs and preserves existing product IDs; no live migration is executed by this PR.
2. Apply `lemnt-server/sql/V20261025__mall_catalog_menu.sql` to the system database. It is repeatable serially and inserts menus/buttons without assigning roles or overwriting existing rows. Assign reviewed permissions through existing role administration. Product editor requires category:list and brand:list, edit/details require corresponding query permissions.
3. Merge the provided mall route into the existing Nacos gateway route list; do not add StripPrefix for mall. Supply the usual mall datasource/Nacos/Dubbo configurations and verify gateway auth plus actual HTTP calls.
4. React registry keys: mall/category/index, mall/brand/index, mall/spu/index. No npm dependency or lockfile changes.
5. Before rollback stop new writes. Old code can continue legacy product reads but does not enforce SPU/taxonomy gates. Do not roll back to old mall binaries while allowing SKU checkout traffic. Retain catalog/ledger rows; no destructive down migration is provided.
