# 商品规格字典关联

2026-10-28，39批次合同收敛修订。本批在既有商品接口中增加规格编号关联，保持现有独立请求/响应、URL及权限。以下请求/响应为构造示例；未进行部署HTTP或浏览器端到端验收。

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
| skus[].properties | array | 是 | Body | 无 | 最多10项；无规格明确传[]，不再接受旧映射写入 |
| skus[].properties[].propertyId | long/string | 是 | Body | 无 | 规格项正整数编号，同SKU不能重复 |
| skus[].properties[].valueId | long/string | 是 | Body | 无 | 正整数编号，必须属于对应规格项 |
| skus[].properties[].propertyName | string | 否 | Body | 无 | 名称由字典读取；如传入须一致，最多64字符 |
| skus[].properties[].valueName | string | 否 | Body | 无 | 值名称由字典读取；如传入须一致，最多100字符 |

列表中的编号使用十进制字符串；后端绑定为Long并校验正数及归属。不能提交属于其他规格的值；同一商品每个SKU选取相同规格项。名称由字典读取，不信任前端名称。

```bash
curl -X POST 'https://<API_HOST>/mall/spus' -H 'Authorization: Bearer <TOKEN>' -H 'Content-Type: application/json' --data '{"code":"SPU-1","name":"示例商品","categoryId":"1","brandId":"2","deliveryTypes":[],"skus":[{"code":"SKU-1","name":"红色款","price":"10.00","stock":5,"properties":[{"propertyId":"3","valueId":"7"}]}]}'
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
| data.skus[].properties | array | 完整规格快照；无规格为[]，旧未关联规格仍保留名称和值 |
| data.skus[].properties[].propertyId/valueId | long/string，可缺失 | 实际保存的字典编号，旧未关联记录不猜测补值 |
| data.skus[].properties[].propertyName/valueName | string | 保存时名称和值 |
| data.skus[].status | string | SKU状态 |

```json
{"code":200,"data":{"id":"10","code":"SPU-1","name":"示例商品","categoryId":"1","brandId":"2","status":"OFF_SHELF","deliveryTypes":[],"skus":[{"id":"11","spuId":"10","code":"SKU-1","name":"红色款","price":"10.00","stock":5,"properties":[{"propertyId":"3","propertyName":"颜色","valueId":"7","valueName":"红色"}],"status":"ON_SHELF"}]}}
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

SKU规格现已合并到 `mall_product_sku.properties`，保存四字段JSON列表；初始化不再创建 `mall_product_sku_spec`，其DO/Mapper/Gateway/XML已移除。DO沿用String字段及Converter解析，目标物理列为LONGTEXT；本地芋道参考SQL不含SKU建表，本批只对照源DO/业务逻辑，未宣称源列长度完全一致。

存量库先核对前序版本（旧表需已有property_id/value_id列，未关联编号保留NULL），停旧写者并备份后审核 `lemnt-server/lemnt-mall/sql/upgrade/sku_properties_json.sql`。脚本复制规格，列出JSON合法性、编号成对、总数量及每个原规格完整匹配核对。核对与备份确认后，再单独审核 `retire_sku_spec_table.sql` 删除旧表；复制脚本不自动删除表。本批没有执行真实MySQL迁移，生成脚本不能作为执行或兼容性证据。

字典修改行为与芋道对齐：PUT `/mall/specs/{id}`（`mall:spec:edit`）修改名称时同步当前SKU的propertyName；PUT `/mall/spec-values/{id}`（`mall:spec-value:edit`）同步valueName。原订单、结算回执和售后快照不变。更新字段仍为原name/remark，无新增请求字段；改名同步失败时字典与SKU整体回滚。DELETE `/mall/spec-values/{id}`（`mall:spec-value:remove`）删除字典值，不清空SKU快照；DELETE `/mall/specs/{id}`（`mall:spec:remove`）在仍有子值时拒绝。它们仍使用既有R/统一异常合同，无新异常处理器。

后台已保存的名称可作为当前编辑快照标签：字典删除后仍展示原名称，允许用户选替代值；实际选项查询失败仍报错，保存必须通过服务端字典存在及归属验证，不把历史标签当成当前有效字典。缺少编号的旧资料仍需人工选择，不按同名推测。

商品管理、App详情、结算、订单和售后均输出四字段Property列表，临时specValueIds字段已移除。变更存储需协调服务版本，旧关系表写者与新SKU JSON写者不能混跑；其他字段和完整业务迁移尚未结束。隔离HTTP测试包含真实Controller/事务/数据库，但不包含网关认证、真实MySQL迁移或浏览器真机验收。
