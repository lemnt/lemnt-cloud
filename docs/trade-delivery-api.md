# 交易配置与配送基础管理接口

> 2026-10-28 配送贯通批次：商品配送字段、地址必填结算、订单运费快照与发货冻结地址见 [配送结算接口变更](delivery-checkout-api.md)。本批已将运费接入下单金额；原文中“尚未接入”的说明仅代表上一批次状态。

2026-10-28 v1。参考本地芋道 config/delivery 模型，Java 17 + Spring Boot 4.1/MyBatis Plus；保持当前项目 Adapter/CQRS/Domain/Gateway 分层。

构造示例，未部署实测；请求必须经网关的管理员会话校验，示例凭证是占位符。R.code=200 成功，成功通常省略 msg；分页使用 rows/total，详情使用 data。Long ID 在不同序列化配置下可为字符串，调用端应按字符串保存；金额字段为整数分，计费量为十进制数（重量千克、体积立方米）。

## 1. 交易配置

接口地址：`/trade/admin/config`。GET（trade:config:query）读取，PUT（trade:config:update）整体保存。请求不能指定 id，服务端固定单例 1。

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| afterSaleRefundReasons | List<String> | 是 | JSON body（PUT） | 无 | 仅退款原因 |
| afterSaleReturnReasons | List<String> | 是 | JSON body（PUT） | 无 | 退货退款原因 |
| deliveryExpressFreeEnabled | Boolean | 是 | JSON body（PUT） | 无 | 是否全场包邮 |
| deliveryExpressFreePrice | Integer | 是 | JSON body（PUT） | 无 | 全场包邮金额门槛，分 |
| deliveryPickUpEnabled | Boolean | 是 | JSON body（PUT） | 无 | 是否开启自提 |
| brokerageEnabled | Boolean | 是 | JSON body（PUT） | 无 | 是否启用分佣 |
| brokerageEnabledCondition | Integer | 是 | JSON body（PUT） | 无 | 分佣模式 |
| brokerageBindMode | Integer | 是 | JSON body（PUT） | 无 | 分销绑定模式 |
| brokeragePosterUrls | List<String> | 是 | JSON body（PUT） | 无 | 分销海报地址 |
| brokerageFirstPercent | Integer | 是 | JSON body（PUT） | 无 | 一级返佣百分比 |
| brokerageSecondPercent | Integer | 是 | JSON body（PUT） | 无 | 二级返佣百分比 |
| brokerageWithdrawMinPrice | Integer | 是 | JSON body（PUT） | 无 | 最低提现金额，分 |
| brokerageWithdrawFeePercent | Integer | 是 | JSON body（PUT） | 无 | 提现手续费百分比 |
| brokerageFrozenDays | Integer | 是 | JSON body（PUT） | 无 | 佣金冻结天数 |
| brokerageWithdrawTypes | List<Integer> | 是 | JSON body（PUT） | 无 | 提现方式 |

响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 成功为 200 |
| data | object / boolean | GET 返回完整配置（含 id 及上述字段）；PUT 返回 true |
| data.id | Long | 单例编号 |
| data.afterSaleRefundReasons | List<String> | 仅退款原因 |
| data.afterSaleReturnReasons | List<String> | 退货退款原因 |
| data.deliveryExpressFreeEnabled | Boolean | 是否全场包邮 |
| data.deliveryExpressFreePrice | Integer | 全场包邮金额门槛，分 |
| data.deliveryPickUpEnabled | Boolean | 是否开启自提 |
| data.brokerageEnabled | Boolean | 是否启用分佣 |
| data.brokerageEnabledCondition | Integer | 分佣模式 |
| data.brokerageBindMode | Integer | 分销绑定模式 |
| data.brokeragePosterUrls | List<String> | 分销海报地址 |
| data.brokerageFirstPercent | Integer | 一级返佣百分比 |
| data.brokerageSecondPercent | Integer | 二级返佣百分比 |
| data.brokerageWithdrawMinPrice | Integer | 最低提现金额，分 |
| data.brokerageWithdrawFeePercent | Integer | 提现手续费百分比 |
| data.brokerageFrozenDays | Integer | 佣金冻结天数 |
| data.brokerageWithdrawTypes | List<Integer> | 提现方式 |

请求示例：
```http
PUT /trade/admin/config
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "afterSaleRefundReasons": [
    "不想要了"
  ],
  "afterSaleReturnReasons": [
    "质量问题"
  ],
  "deliveryExpressFreeEnabled": false,
  "deliveryExpressFreePrice": 0,
  "deliveryPickUpEnabled": false,
  "brokerageEnabled": false,
  "brokerageEnabledCondition": 1,
  "brokerageBindMode": 1,
  "brokeragePosterUrls": [],
  "brokerageFirstPercent": 0,
  "brokerageSecondPercent": 0,
  "brokerageWithdrawMinPrice": 0,
  "brokerageWithdrawFeePercent": 0,
  "brokerageFrozenDays": 0,
  "brokerageWithdrawTypes": []
}
```

GET 成功响应：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "afterSaleRefundReasons": [
      "不想要了"
    ],
    "afterSaleReturnReasons": [
      "质量问题"
    ],
    "deliveryExpressFreeEnabled": false,
    "deliveryExpressFreePrice": 0,
    "deliveryPickUpEnabled": false,
    "brokerageEnabled": false,
    "brokerageEnabledCondition": 1,
    "brokerageBindMode": 1,
    "brokeragePosterUrls": [],
    "brokerageFirstPercent": 0,
    "brokerageSecondPercent": 0,
    "brokerageWithdrawMinPrice": 0,
    "brokerageWithdrawFeePercent": 0,
    "brokerageFrozenDays": 0,
    "brokerageWithdrawTypes": []
  }
}
```

PUT 成功响应：
```json
{"code":200,"data":true}
```

分销和自提字段按参考保留并保存，本批不代表对应功能流程已实现；修改配置必须提交完整列表，空列表为显式清空。

## 2. 快递公司

接口地址：`/trade/admin/delivery/expresses`。

| 方法 | 路径 | 权限 | 请求/响应 |
| --- | --- | --- | --- |
| POST | /trade/admin/delivery/expresses | trade:delivery-express:create | 独立新增请求，R<Long> 新编号 |
| PUT | /trade/admin/delivery/expresses/{id} | trade:delivery-express:update | 独立修改请求，R<Boolean> |
| DELETE | /trade/admin/delivery/expresses/{id} | trade:delivery-express:delete | 逻辑删除，R<Boolean> |
| GET | /trade/admin/delivery/expresses/{id} | trade:delivery-express:query | 详情，R<独立DetailRes> |
| GET | /trade/admin/delivery/expresses | trade:delivery-express:list | 分页，R<独立PageRes> 的 rows/total |
| GET | /trade/admin/delivery/expresses/simple | trade:delivery-express:query | 选择列表，R<List<独立SimpleRes>> |

请求参数：

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | Long | 修改/删除/详情是 | path | 无 | 主记录编号 |
| name | String | 否 | query（GET分页） | 无 | 名称模糊查询，最多100字 |
| pageNum | Integer | 否 | query | 1 | 从1开始 |
| pageSize | Integer | 否 | query | 20 | 1至100 |
| status | Integer | 否 | query | 无 | 0启用/1停用 |
| code | String | 是 | JSON body | 无 | 快递公司编码 |
| name | String | 是 | JSON body | 无 | 快递公司名称 |
| logo | String | 否 | JSON body | 无 | 图标地址 |
| sort | Integer | 是 | JSON body | 无 | 排序 |
| status | Integer | 是 | JSON body | 无 | 状态，0 启用、1 停用 |

响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 成功200 |
| data | Long / boolean / object / array | 按新增、写操作、详情、选择列表区分 |
| rows | array | 分页结果，元素字段见下 |
| total | Long | 分页总数 |
| data.id / rows[].id | Long | 编号 |
| data.code / rows[].code | String | 快递公司编码 |
| data.name / rows[].name | String | 快递公司名称 |
| data.logo / rows[].logo | String | 图标地址 |
| data.sort / rows[].sort | Integer | 排序 |
| data.status / rows[].status | Integer | 状态，0 启用、1 停用 |

选择列表元素只含 id/name/code；快递公司仅返回启用项。分页不包含模板子规则，详情才返回完整规则。

新增请求：
```http
POST /trade/admin/delivery/expresses
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "code": "SF",
  "name": "顺丰",
  "logo": "https://example.test/sf.png",
  "sort": 0,
  "status": 0
}
```

新增响应：
```json
{"code":200,"data":7}
```

详情请求：
```http
GET /trade/admin/delivery/expresses/7
Authorization: Bearer <ADMIN_TOKEN>
```

详情响应：
```json
{
  "code": 200,
  "data": {
    "id": 7,
    "code": "SF",
    "name": "顺丰",
    "logo": "https://example.test/sf.png",
    "sort": 0,
    "status": 0
  }
}
```

分页请求：
```http
GET /trade/admin/delivery/expresses?name=顺丰&pageNum=1&pageSize=20
Authorization: Bearer <ADMIN_TOKEN>
```

分页响应：
```json
{
  "code": 200,
  "rows": [
    {
      "id": 7,
      "code": "SF",
      "name": "顺丰",
      "logo": "https://example.test/sf.png",
      "sort": 0,
      "status": 0
    }
  ],
  "total": 1
}
```

修改使用 PUT /trade/admin/delivery/expresses/7，body 与新增同字段。模板修改传完整规则，已有子项带 id，省略的旧子项删除；跨模板引用子项 id 或重复区域拒绝，主从表一起回滚。DELETE /trade/admin/delivery/expresses/7 不带 body。二者成功均返回 `{"code":200,"data":true}`。选择列表请求 GET /trade/admin/delivery/expresses/simple，同样携带管理员会话；成功为 `{"code":200,"data":[{"id": 7, "code": "SF", "name": "顺丰"}]}`。

## 3. 运费模板

接口地址：`/trade/admin/delivery/templates`。

| 方法 | 路径 | 权限 | 请求/响应 |
| --- | --- | --- | --- |
| POST | /trade/admin/delivery/templates | trade:delivery-template:create | 独立新增请求，R<Long> 新编号 |
| PUT | /trade/admin/delivery/templates/{id} | trade:delivery-template:update | 独立修改请求，R<Boolean> |
| DELETE | /trade/admin/delivery/templates/{id} | trade:delivery-template:delete | 逻辑删除，R<Boolean> |
| GET | /trade/admin/delivery/templates/{id} | trade:delivery-template:query | 详情，R<独立DetailRes> |
| GET | /trade/admin/delivery/templates | trade:delivery-template:list | 分页，R<独立PageRes> 的 rows/total |
| GET | /trade/admin/delivery/templates/simple | trade:delivery-template:query | 选择列表，R<List<独立SimpleRes>> |

请求参数：

| 参数 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | Long | 修改/删除/详情是 | path | 无 | 主记录编号 |
| name | String | 否 | query（GET分页） | 无 | 名称模糊查询，最多100字 |
| pageNum | Integer | 否 | query | 1 | 从1开始 |
| pageSize | Integer | 否 | query | 20 | 1至100 |
| name | String | 是 | JSON body | 无 | 模板名称 |
| chargeMode | Integer | 是 | JSON body | 无 | 计费方式，1 件数、2 重量千克、3 体积立方米 |
| sort | Integer | 是 | JSON body | 无 | 排序 |
| charges | array | 是 | JSON body | 无 | 至少一组计费规则，最多100组 |
| frees | array | 是 | JSON body | 无 | 包邮规则，允许空列表，最多100组 |
| charges[].id | Long | 否 | JSON body | 无 | 编号；新增不带，修改保留已有子项编号 |
| charges[].areaIds | List<Integer> | 是 | JSON body | 无 | 配送区域编号集合 |
| charges[].startCount | BigDecimal | 是 | JSON body | 无 | 首量 |
| charges[].startPrice | Integer | 是 | JSON body | 无 | 首价，分 |
| charges[].extraCount | BigDecimal | 是 | JSON body | 无 | 续量 |
| charges[].extraPrice | Integer | 是 | JSON body | 无 | 续价，分 |
| frees[].id | Long | 否 | JSON body | 无 | 编号；新增不带，修改保留已有子项编号 |
| frees[].areaIds | List<Integer> | 是 | JSON body | 无 | 配送区域编号集合 |
| frees[].freePrice | Integer | 是 | JSON body | 无 | 包邮金额门槛，分 |
| frees[].freeCount | Integer | 是 | JSON body | 无 | 包邮计费量门槛 |

响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 成功200 |
| data | Long / boolean / object / array | 按新增、写操作、详情、选择列表区分 |
| rows | array | 分页结果，元素字段见下 |
| total | Long | 分页总数 |
| data.id / rows[].id | Long | 编号 |
| data.name / rows[].name | String | 模板名称 |
| data.chargeMode / rows[].chargeMode | Integer | 计费方式，1 件数、2 重量千克、3 体积立方米 |
| data.sort / rows[].sort | Integer | 排序 |
| data.charges | array | 详情完整计费规则 |
| data.frees | array | 详情完整包邮规则 |
| data.charges[].id | Long | 编号 |
| data.charges[].templateId | Long | 模板编号 |
| data.charges[].areaIds | List<Integer> | 配送区域编号集合 |
| data.charges[].chargeMode | Integer | 模板计费方式快照 |
| data.charges[].startCount | BigDecimal | 首量 |
| data.charges[].startPrice | Integer | 首价，分 |
| data.charges[].extraCount | BigDecimal | 续量 |
| data.charges[].extraPrice | Integer | 续价，分 |
| data.frees[].id | Long | 编号 |
| data.frees[].templateId | Long | 模板编号 |
| data.frees[].areaIds | List<Integer> | 配送区域编号集合 |
| data.frees[].freePrice | Integer | 包邮金额门槛，分 |
| data.frees[].freeCount | Integer | 包邮计费量门槛 |

选择列表元素只含 id/name；快递公司仅返回启用项。分页不包含模板子规则，详情才返回完整规则。

新增请求：
```http
POST /trade/admin/delivery/templates
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "name": "北京重量模板",
  "chargeMode": 2,
  "sort": 0,
  "charges": [
    {
      "areaIds": [
        110101
      ],
      "startCount": 1.5,
      "startPrice": 600,
      "extraCount": 0.5,
      "extraPrice": 100
    }
  ],
  "frees": [
    {
      "areaIds": [
        110101
      ],
      "freePrice": 20000,
      "freeCount": 3
    }
  ]
}
```

新增响应：
```json
{"code":200,"data":7}
```

详情请求：
```http
GET /trade/admin/delivery/templates/7
Authorization: Bearer <ADMIN_TOKEN>
```

详情响应：
```json
{
  "code": 200,
  "data": {
    "id": 7,
    "name": "北京重量模板",
    "chargeMode": 2,
    "sort": 0,
    "charges": [
      {
        "id": 11,
        "areaIds": [
          110101
        ],
        "startCount": 1.5,
        "startPrice": 600,
        "extraCount": 0.5,
        "extraPrice": 100,
        "templateId": 7,
        "chargeMode": 2
      }
    ],
    "frees": [
      {
        "id": 12,
        "areaIds": [
          110101
        ],
        "freePrice": 20000,
        "freeCount": 3,
        "templateId": 7
      }
    ]
  }
}
```

分页请求：
```http
GET /trade/admin/delivery/templates?name=北京&pageNum=1&pageSize=20
Authorization: Bearer <ADMIN_TOKEN>
```

分页响应：
```json
{
  "code": 200,
  "rows": [
    {
      "id": 7,
      "name": "北京重量模板",
      "chargeMode": 2,
      "sort": 0
    }
  ],
  "total": 1
}
```

修改使用 PUT /trade/admin/delivery/templates/7，body 与新增同字段。模板修改传完整规则，已有子项带 id，省略的旧子项删除；跨模板引用子项 id 或重复区域拒绝，主从表一起回滚。DELETE /trade/admin/delivery/templates/7 不带 body。二者成功均返回 `{"code":200,"data":true}`。选择列表请求 GET /trade/admin/delivery/templates/simple，同样携带管理员会话；成功为 `{"code":200,"data":[{"id": 7, "name": "北京重量模板"}]}`。

## 4. 计价规则与错误

运费计算目前是应用服务 DeliveryPriceService，输入是服务端商品快照，不对会员暴露允许提交价格的报价接口。

- 同一模板合并件数/重量/体积；首量内收首价，超出部分按续量向上取整。
- 地区包邮同时达到金额与计费量门槛；全场包邮和活动包邮优先。
- 分摊采用整数分，前面商品行向下取整，最后一行接收尾差，合计精确等于模板运费。
- 不存在的地区计费规则、模板以及必要重量/体积缺失会报错。
- 新发货必须选择已启用快递公司；历史运单重复请求先核验原请求，停用或删除公司不改变历史发货结果。

复用当前统一异常处理：Bean Validation 返回 HTTP 422 / application/problem+json；ServiceException(code=400) 返回 HTTP 400 / application/problem+json。成功仍使用 R，失败使用 ProblemDetail，不能把错误按 R.data 解析。构造业务错误示例：
```json
{"type":"urn:lemnt:problem:business-rule-violation","title":"业务规则校验失败","status":400,"detail":"配送区域不合法或重复","instance":"/trade/admin/delivery/templates","code":"BUSINESS_RULE_VIOLATION"}
```

空详情返回 R.ok(null)，默认序列化可能省略 data；空分页为 `{"code":200,"rows":[],"total":0}`。未授权由网关及公共权限组件拒绝，不在本模块另写认证逻辑。

## 5. 上线和未完成部分

先在演练库执行 `lemnt-trade/sql/V7__delivery_config.sql`，再部署；初始化单例配置，快递公司和运费模板由管理接口维护。存量发货编码先核对再导入公司表，不能自动把未知编码当启用渠道。权限脚本只增加权限定义，不自动授权角色或伪造管理页面。

未执行真实数据库变更。未新增配置管理页面。商品 delivery_types/delivery_template_id、SKU weight/volume、会员配送地址及结算/订单运费快照尚未贯通，本批不代表运费已经加入当前订单金额。下一阶段须贯通这些字段后验证下单总价及退款分摊。真实渠道 SDK 后续接入。

## 6. 2026-10-31 后台页面接通（第130批）

现已新增快递公司、运费模板、交易配置三个后台入口，组件键分别为 `trade/delivery/express/index`、`trade/delivery/express-template/index`、`trade/config/index`。沿用以上已有接口，保留 `/trade` 服务前缀；地区树使用 `/system/areas/tree`，网关按已有服务路由转发。

快递公司支持名称、状态筛选及分页、详情、新增、编辑、删除；状态0启用、1停用。运费模板支持名称筛选、分页、详情、新增编辑删除，配置按件、重量kg、体积m³的计费区域和包邮区域。地区勾选使用真实地区树，默认展开上级选择为子地区；服务端按提交地区编号精确匹配。金额以元文本编辑、BigInt精确转整数分；按件要求整数数量，重量体积最多六位小数。包邮须同时达到数量与金额门槛。编辑先读取完整详情，保留已有子规则ID；移除规则后提交由原服务删除，不重建所有规则。同类规则地区重复会拒绝保存。

交易配置页面仅开放仅退款原因、退货退款原因、全场满额包邮开关及门槛。每行一个原因，最多100条，每条200字且不得重复，留空可清空。PUT为全量契约，必须先成功GET单例配置，自提、分佣等未开放编辑字段按原值保留，不猜测默认值。配置缺失时提示初始化，不允许提交空配置；关闭聚焦/重连自动刷新，避免编辑中表单被后台读取覆盖。

页面列表、详情、各操作分别使用现有权限；失败允许重试，详情失败不开空表单，响应false不会提示成功。新增系统库脚本 `lemnt-server/sql/V20261031__trade_delivery_config_menu.sql` 只登记菜单/按钮，不授予角色、不新增业务表；重复执行保留原交易根名称和角色绑定。仅H2演练，未执行真实库。

此节更新此前“未新增配置管理页面”的历史状态。已有结算运费衔接仍沿用当前服务，本批增加管理入口及契约验证；不代表浏览器交互、真实环境下单或渠道联调已验收。分佣与自提页面、快递批量导入导出等源功能仍未在本批补齐。
