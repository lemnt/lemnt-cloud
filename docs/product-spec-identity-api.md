# 商品规格字典关联

2026-10-28。本批在既有商品接口中增加规格编号关联，保持现有独立请求/响应、URL及权限。以下请求/响应为构造示例；未进行部署HTTP或浏览器端到端验收。

## 1. 创建商品

接口地址：`/mall/spus`。请求方式：POST。接口权限码：`mall:spu:add`。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 已认证令牌 |
| code | string | 是 | Body | 无 | SPU编码，最多64字符 |
| name | string | 是 | Body | 无 | 商品名称，最多200字符 |
| categoryId | long/string | 是 | Body | 无 | 有效分类编号 |
| brandId | long/string | 是 | Body | 无 | 有效品牌编号 |
| description | string | 否 | Body | 无 | 最多65535字符 |
| image | string | 否 | Body | 无 | 主图URL，最多500字符 |
| deliveryTypes | integer[] | 是 | Body | 无 | 1快递、2自提；快递需配置模板 |
| deliveryTemplateId | long/string | 否 | Body | 无 | 运费模板 |
| skus | object[] | 是 | Body | 无 | 1至100个SKU |
| skus[].code/name | string | 是 | Body | 无 | 编码64字符、名称200字符 |
| skus[].price | decimal | 是 | Body | 无 | 元，正数，两位小数 |
| skus[].stock | integer | 是 | Body | 无 | 可用库存，不小于0 |
| skus[].image | string | 否 | Body | 无 | SKU图片，最多500字符 |
| skus[].weight/volume | decimal | 否 | Body | 无 | 千克/立方米，非负，最多六位小数 |
| skus[].specValueIds | object | 新后台使用 | Body | 无 | 规格编号→规格值编号，最多10项；{}为无规格，缺失为旧名值调用 |
| skus[].properties | object | 否 | Body | 空映射 | 兼容旧名称和值；同时提交编号与非空名值时，必须与服务端字典完全一致 |

编号键、值均使用十进制字符串；后端绑定为Long并校验正数及归属。不能提交属于其他规格的值；同一商品每个SKU选取相同规格项。名称由字典读取，不信任前端名称。

```bash
curl -X POST 'https://<API_HOST>/mall/spus' -H 'Authorization: Bearer <TOKEN>' -H 'Content-Type: application/json' --data '{"code":"SPU-1","name":"示例商品","categoryId":"1","brandId":"2","deliveryTypes":[],"skus":[{"code":"SKU-1","name":"红色款","price":"10.00","stock":5,"properties":{},"specValueIds":{"3":"7"}}]}'
```

| 响应字段 | 类型 | 说明 |
|---|---|---|
| code/msg | integer/string，可缺失 | 原R成功合同；msg为空时省略 |
| data.id/code/name | long/string/string | SPU身份及名称 |
| data.categoryId/brandId | long/string | 分类与品牌 |
| data.status | string | 新建OFF_SHELF |
| data.description/image/deliveryTemplateId | string/long，可缺失 | 可空字段按现有序列化配置省略 |
| data.deliveryTypes | integer[] | 配送方式 |
| data.skus | object[] | 保存后的商品SKU |
| data.skus[].id/spuId | long/string | SKU和所属SPU编号 |
| data.skus[].code/name | string | SKU编码、名称 |
| data.skus[].price/stock | string/integer | 元金额字符串、库存 |
| data.skus[].image/weight/volume | string，可缺失 | 图片、千克、立方米 |
| data.skus[].properties | object | 原名称和值快照 |
| data.skus[].specValueIds | object，可缺失 | 实际保存的字典编号关联；旧未知为null或省略，无规格为{} |
| data.skus[].status | string | SKU状态 |

```json
{"code":200,"data":{"id":"10","code":"SPU-1","name":"示例商品","categoryId":"1","brandId":"2","status":"OFF_SHELF","deliveryTypes":[],"skus":[{"id":"11","spuId":"10","code":"SKU-1","name":"红色款","price":"10.00","stock":5,"properties":{"颜色":"红色"},"specValueIds":{"3":"7"},"status":"ON_SHELF"}]}}
```

更新使用 PUT `/mall/spus/{id}`，权限 `mall:spu:edit`，Body沿用现有更新合同（没有SPU code；已有SKU带id），SKU的新增字段相同；成功为原R<boolean>。详情 GET `/mall/spus/{id}`，权限`mall:spu:query`，返回同样的SKU关联字段；SKU详情、批量商品详情及App商品详情独立响应均透传编号关联。接口不返回DO。

业务错误走统一ProblemDetail，规格不存在、值不存在、跨规格值、不同SKU规格项集合或伪造名称为ServiceException400；请求字段校验沿用统一422。构造错误示例：

```json
{"type":"urn:lemnt:problem:business-rule-violation","title":"业务规则校验失败","status":400,"detail":"规格值不属于所选规格项","instance":"/mall/spus","code":"BUSINESS_RULE_VIOLATION"}
```

## 2. 后台选择项

GET `/mall/specs/simple`（`mall:spec:query`）与GET `/mall/spec-values/simple`（`mall:spec-value:query`）。Authorization必填Header；name为可选Query关键词；limit为可选Query整数，默认20、最多100；规格值查询的specId为必填Query正整数。返回原R<data数组>，规格元素id/name，规格值元素id/specId/name。无匹配返回data:[]。后台按关键字搜索，并通过已有详情接口补入已选项，不把首屏100条当全部字典。

```bash
curl 'https://<API_HOST>/mall/spec-values/simple?specId=3&name=%E7%BA%A2&limit=100' -H 'Authorization: Bearer <TOKEN>'
```

```json
{"code":200,"data":[{"id":"7","specId":"3","name":"红色"}]}
```

## 3. 迁移边界

现有mall_product_sku_spec增加property_id/value_id和关联查询索引，未新增表。升级脚本：`lemnt-server/lemnt-mall/sql/upgrade/product_sku_spec_identity.sql`，需停旧写者、备份并核对版本后执行一次；本次未执行真实库升级。旧名值保留，禁止同名猜测ID；后台编辑旧商品需人工选择字典，不静默清空原规格。

当前HTTP仍保留properties名值映射，并新增specValueIds；与芋道properties中包含propertyId/propertyName/valueId/valueName的列表尚未完全同构。原订单/售后历史保持上一批名值快照，本批没有伪造其字典ID；跨结算/订单结构化Property列表属于后续迁移范围。
