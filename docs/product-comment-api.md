# 商品评价接口

2026-10-29。本批迁移评价多维评分、图片/匿名、会员与订单商品快照、公开查询、后台管理及小程序调用。订单项编号和订单评价标记联动已接入，见[订单项评价联动](order-item-comment-api.md)；虚拟评价创建、自动评价、照片上传/相机选择及统计/导出仍未迁移，不能视为芋道评价功能已全量完成。

以下为按当前代码核对的构造示例，非部署HTTP实测。成功仍使用R；空值按NON_NULL省略，空数组和false保留。安全范围内Long为JSON数字，长编号为字符串；客户端统一保存字符串。响应日期格式yyyy-MM-dd HH:mm:ss，无附加时区。错误为统一RFC9457 Problem Details、application/problem+json。

## 1. 后台评价分页

接口地址：`/mall/comments`；请求方式：`GET`；权限：`mall:comment:list`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|spuId|integer|string|否|Query|无|正数SPU筛选|
|memberId|integer|string|否|Query|无|正数会员筛选|
|score|integer|否|Query|无|1至5精确匹配|
|visible|boolean|否|Query|无|false表示隐藏，未传不过滤|
|replied|boolean|否|Query|无|false表示未回复，未传不过滤|
|userNickname|string|否|Query|无|评价人昵称快照模糊匹配，最多255字，空白忽略|
|orderId|integer或string|否|Query|无|真实交易订单ID，正数，不是订单号|
|spuName|string|否|Query|无|购买商品名称快照模糊匹配，最多255字，空白忽略|
|createTimeStart|string|否|Query|无|创建时间起，ISO本地时间，例如2026-10-29T00:00:00，包含边界|
|createTimeEnd|string|否|Query|无|创建时间止，包含边界，可单独提供|
|pageNum|integer|否|Query|1|1至1000000|
|pageSize|integer|否|Query|20|1至100|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|rows|array|分页行，空页[]|
|total|integer|同一筛选范围的总数|
|rows[].id|integer|string|评价编号|
|rows[].spuId|integer|string|商品SPU编号|
|rows[].skuId|integer|string|SKU编号|
|rows[].orderNo|string|原订单号|
|rows[].memberId|integer|string|会员编号，仅后台|
|rows[].score|integer|综合评分1至5|
|rows[].content|string|评价正文，最多1000字符|
|rows[].visible|boolean|公开显示标记，仅后台|
|rows[].replyContent|string，可空|商家回复|
|rows[].replyUserId|integer|string，可空|回复管理员编号，仅后台|
|rows[].replyTime|string，可空|回复时间|
|rows[].createTime|string|评价时间|
|rows[].descriptionScores|integer，可空|描述评分1至5，历史未知为空|
|rows[].benefitScores|integer，可空|服务评分1至5，历史未知为空|
|rows[].picUrls|string[]，可空|最多9张HTTPS图片，旧记录未知为空|
|rows[].anonymous|boolean，可空|匿名标记，旧记录未知为空|
|rows[].userNickname|string，可空|保存时会员昵称；公开匿名固定匿名用户，历史缺失不补造|
|rows[].userAvatar|string，可空|保存时头像；公开匿名省略|
|rows[].orderId|integer|string，可空|原订单数据库编号，仅后台|
|rows[].spuName|string，可空|原订单商品名称|
|rows[].skuPicUrl|string，可空|原订单图片|
|rows[].skuProperties|array，可空|原订单规格快照|
|rows[].replyStatus|boolean|是否商家回复|
|rows[].picUrls[]|string|HTTPS图片地址|
|rows[].skuProperties[].propertyId|integer或string，可空|规格编号，历史未知为空|
|rows[].skuProperties[].propertyName|string|规格名称|
|rows[].skuProperties[].valueId|integer或string，可空|规格值编号，历史未知为空|
|rows[].skuProperties[].valueName|string|规格值名称|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/comments?visible=false&replied=false&pageNum=1&pageSize=20' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "rows": [
    {
      "id": 9,
      "spuId": 2,
      "skuId": 3,
      "orderNo": "ORD1",
      "orderId": 10,
      "memberId": 1,
      "score": 4,
      "descriptionScores": 5,
      "benefitScores": 4,
      "content": "真实购买评价",
      "visible": true,
      "anonymous": true,
      "userNickname": "保存时昵称",
      "userAvatar": "https://example.test/avatar.png",
      "picUrls": [
        "https://example.test/review.png"
      ],
      "spuName": "购买时商品名称",
      "skuPicUrl": "https://example.test/product.png",
      "skuProperties": [
        {
          "propertyId": 8,
          "propertyName": "颜色",
          "valueId": 9,
          "valueName": "红色"
        }
      ],
      "replyStatus": false,
      "createTime": "2026-10-29 10:00:00"
    }
  ],
  "total": 1
}
```

## 2. 后台评价详情

接口地址：`/mall/comments/{id}`；请求方式：`GET`；权限：`mall:comment:query`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|integer|string|是|Path|无|评价编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|data|object|本接口独立响应对象|
|data.id|integer|string|评价编号|
|data.spuId|integer|string|商品SPU编号|
|data.skuId|integer|string|SKU编号|
|data.orderNo|string|原订单号|
|data.memberId|integer|string|会员编号，仅后台|
|data.score|integer|综合评分1至5|
|data.content|string|评价正文，最多1000字符|
|data.visible|boolean|公开显示标记，仅后台|
|data.replyContent|string，可空|商家回复|
|data.replyUserId|integer|string，可空|回复管理员编号，仅后台|
|data.replyTime|string，可空|回复时间|
|data.createTime|string|评价时间|
|data.descriptionScores|integer，可空|描述评分1至5，历史未知为空|
|data.benefitScores|integer，可空|服务评分1至5，历史未知为空|
|data.picUrls|string[]，可空|最多9张HTTPS图片，旧记录未知为空|
|data.anonymous|boolean，可空|匿名标记，旧记录未知为空|
|data.userNickname|string，可空|保存时会员昵称；公开匿名固定匿名用户，历史缺失不补造|
|data.userAvatar|string，可空|保存时头像；公开匿名省略|
|data.orderId|integer|string，可空|原订单数据库编号，仅后台|
|data.spuName|string，可空|原订单商品名称|
|data.skuPicUrl|string，可空|原订单图片|
|data.skuProperties|array，可空|原订单规格快照|
|data.replyStatus|boolean|是否商家回复|
|data.picUrls[]|string|HTTPS图片地址|
|data.skuProperties[].propertyId|integer或string，可空|规格编号，历史未知为空|
|data.skuProperties[].propertyName|string|规格名称|
|data.skuProperties[].valueId|integer或string，可空|规格值编号，历史未知为空|
|data.skuProperties[].valueName|string|规格值名称|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/comments/9' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": 9,
    "spuId": 2,
    "skuId": 3,
    "orderNo": "ORD1",
    "orderId": 10,
    "memberId": 1,
    "score": 4,
    "descriptionScores": 5,
    "benefitScores": 4,
    "content": "真实购买评价",
    "visible": true,
    "anonymous": true,
    "userNickname": "保存时昵称",
    "userAvatar": "https://example.test/avatar.png",
    "picUrls": [
      "https://example.test/review.png"
    ],
    "spuName": "购买时商品名称",
    "skuPicUrl": "https://example.test/product.png",
    "skuProperties": [
      {
        "propertyId": 8,
        "propertyName": "颜色",
        "valueId": 9,
        "valueName": "红色"
      }
    ],
    "replyStatus": false,
    "createTime": "2026-10-29 10:00:00"
  }
}
```

## 3. 商家回复

接口地址：`/mall/comments/{id}/reply`；请求方式：`PUT`；权限：`mall:comment:reply`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|integer|string|是|Path|无|评价编号|
|content|string|是|Body|无|非空，最多1000字符，前后空白裁剪|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|data|object|本接口独立响应对象|
|data.id|integer|string|评价编号|
|data.spuId|integer|string|商品SPU编号|
|data.skuId|integer|string|SKU编号|
|data.orderNo|string|原订单号|
|data.memberId|integer|string|会员编号，仅后台|
|data.score|integer|综合评分1至5|
|data.content|string|评价正文，最多1000字符|
|data.visible|boolean|公开显示标记，仅后台|
|data.replyContent|string，可空|商家回复|
|data.replyUserId|integer|string，可空|回复管理员编号，仅后台|
|data.replyTime|string，可空|回复时间|
|data.createTime|string|评价时间|
|data.descriptionScores|integer，可空|描述评分1至5，历史未知为空|
|data.benefitScores|integer，可空|服务评分1至5，历史未知为空|
|data.picUrls|string[]，可空|最多9张HTTPS图片，旧记录未知为空|
|data.anonymous|boolean，可空|匿名标记，旧记录未知为空|
|data.userNickname|string，可空|保存时会员昵称；公开匿名固定匿名用户，历史缺失不补造|
|data.userAvatar|string，可空|保存时头像；公开匿名省略|
|data.orderId|integer|string，可空|原订单数据库编号，仅后台|
|data.spuName|string，可空|原订单商品名称|
|data.skuPicUrl|string，可空|原订单图片|
|data.skuProperties|array，可空|原订单规格快照|
|data.replyStatus|boolean|是否商家回复|
|data.picUrls[]|string|HTTPS图片地址|
|data.skuProperties[].propertyId|integer或string，可空|规格编号，历史未知为空|
|data.skuProperties[].propertyName|string|规格名称|
|data.skuProperties[].valueId|integer或string，可空|规格值编号，历史未知为空|
|data.skuProperties[].valueName|string|规格值名称|

请求示例：
```sh
curl -X PUT 'https://<API_HOST>/mall/comments/9/reply' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"content": "感谢您的反馈"}'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": 9,
    "spuId": 2,
    "skuId": 3,
    "orderNo": "ORD1",
    "orderId": 10,
    "memberId": 1,
    "score": 4,
    "descriptionScores": 5,
    "benefitScores": 4,
    "content": "真实购买评价",
    "visible": true,
    "anonymous": true,
    "userNickname": "保存时昵称",
    "userAvatar": "https://example.test/avatar.png",
    "picUrls": [
      "https://example.test/review.png"
    ],
    "spuName": "购买时商品名称",
    "skuPicUrl": "https://example.test/product.png",
    "skuProperties": [
      {
        "propertyId": 8,
        "propertyName": "颜色",
        "valueId": 9,
        "valueName": "红色"
      }
    ],
    "replyStatus": true,
    "createTime": "2026-10-29 10:00:00",
    "replyContent": "感谢您的反馈",
    "replyUserId": 99,
    "replyTime": "2026-10-29 10:01:00"
  }
}
```

## 4. 显隐设置

接口地址：`/mall/comments/{id}/visible`；请求方式：`PUT`；权限：`mall:comment:edit`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|integer|string|是|Path|无|评价编号|
|visible|boolean|是|Body|无|true显示，false隐藏|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|data|object|本接口独立响应对象|
|data.success|boolean|显隐修改成功true|

请求示例：
```sh
curl -X PUT 'https://<API_HOST>/mall/comments/9/visible' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"visible": false}'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 5. 商品公开评价分页

接口地址：`/mall/app/comments`；请求方式：`GET`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|spuId|integer|string|是|Query|无|正数商品SPU编号|
|type|integer|是|Query|无|0全部、1好评>=4、2中评=3、3差评<3|
|pageNum|integer|否|Query|1|1至1000000|
|pageSize|integer|否|Query|20|1至100|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|rows|array|分页行，空页[]|
|total|integer|同一筛选范围的总数|
|rows[].id|integer|string|评价编号|
|rows[].spuId|integer|string|商品SPU编号|
|rows[].skuId|integer|string|SKU编号|
|rows[].score|integer|综合评分1至5|
|rows[].content|string|评价正文，最多1000字符|
|rows[].replyContent|string，可空|商家回复|
|rows[].replyTime|string，可空|回复时间|
|rows[].createTime|string|评价时间|
|rows[].descriptionScores|integer，可空|描述评分1至5，历史未知为空|
|rows[].benefitScores|integer，可空|服务评分1至5，历史未知为空|
|rows[].picUrls|string[]，可空|最多9张HTTPS图片，旧记录未知为空|
|rows[].anonymous|boolean，可空|匿名标记，旧记录未知为空|
|rows[].userNickname|string，可空|保存时会员昵称；公开匿名固定匿名用户，历史缺失不补造|
|rows[].userAvatar|string，可空|保存时头像；公开匿名省略|
|rows[].spuName|string，可空|原订单商品名称|
|rows[].skuPicUrl|string，可空|原订单图片|
|rows[].skuProperties|array，可空|原订单规格快照|
|rows[].replyStatus|boolean|是否商家回复|
|rows[].picUrls[]|string|HTTPS图片地址|
|rows[].skuProperties[].propertyId|integer或string，可空|规格编号，历史未知为空|
|rows[].skuProperties[].propertyName|string|规格名称|
|rows[].skuProperties[].valueId|integer或string，可空|规格值编号，历史未知为空|
|rows[].skuProperties[].valueName|string|规格值名称|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/app/comments?spuId=2&type=0&pageNum=1&pageSize=20'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "rows": [
    {
      "id": 9,
      "spuId": 2,
      "skuId": 3,
      "score": 4,
      "descriptionScores": 5,
      "benefitScores": 4,
      "content": "真实购买评价",
      "anonymous": true,
      "userNickname": "匿名用户",
      "picUrls": [
        "https://example.test/review.png"
      ],
      "spuName": "购买时商品名称",
      "skuPicUrl": "https://example.test/product.png",
      "skuProperties": [
        {
          "propertyId": 8,
          "propertyName": "颜色",
          "valueId": 9,
          "valueName": "红色"
        }
      ],
      "replyStatus": false,
      "createTime": "2026-10-29 10:00:00"
    }
  ],
  "total": 1
}
```

## 6. 评价本人完成订单商品

接口地址：`/mall/app/comments`；请求方式：`POST`；权限：`会员网关认证，无后台权限码`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <MEMBER_TOKEN>|
|orderNo|string|是|Body|无|本人COMPLETED订单号，非空，最多40字符|
|skuId|integer|string|是|Body|无|订单内正数SKU编号|
|score|integer|条件必填|Body|无|兼容原单分输入1至5：未传两项评分时必填；同时传时必须等于两项平均向下取整|
|descriptionScores|integer|条件必填|Body|无|1至5，必须与benefitScores成对|
|benefitScores|integer|条件必填|Body|无|1至5，必须与descriptionScores成对|
|content|string|是|Body|无|非空，最多1000字符，前后空白裁剪|
|anonymous|boolean|否|Body|false|是否匿名公开展示|
|picUrls|string[]|否|Body|[]|最多9个非空HTTPS地址，每项最多1024字符|

|响应字段|类型|说明|
|---|---|---|
|code|integer|成功200|
|msg|string，可空|成功通常省略|
|data|object|本接口独立响应对象|
|data.id|integer或string|稳定评价编号|

请求示例：
```sh
curl -X POST 'https://<API_HOST>/mall/app/comments' -H 'Authorization: Bearer <MEMBER_TOKEN>' -H 'Content-Type: application/json' --data '{"orderNo": "ORD1", "skuId": 3, "descriptionScores": 5, "benefitScores": 4, "content": "真实购买评价", "anonymous": true, "picUrls": ["https://example.test/review.png"]}'
```

成功示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": 9
  }
}
```

分页时间范围倒置返回400业务错误，非法日期、负数订单ID及超长名称返回422。所有已提供条件同时生效；昵称和商品名查评价保存的历史快照，不用当前商品/会员名补值。`replied`匹配存储的`reply_status`，不以回复正文是否为空判断。后台新增输入及时间范围控件，响应结构不变。

## 7. 存储与业务语义

仅扩展mall_product_comment，不新增表。新字段：description_scores、benefit_scores、pic_urls、anonymous、user_nickname、user_avatar、order_id、spu_name、sku_pic_url、sku_properties、reply_status、deleted。JSON在基础设施String DO和Converter之间转换，不把Jackson依赖带入domain/application。锁XML有完整resultMap；MyBatis Plus逻辑删除与visible前台显隐分别处理。

综合评分=floor((描述评分+服务评分)/2)。旧客户端仅传score时，明确将它作为两项相同的本次输入；存量记录的两项评分、图片、匿名、昵称头像和商品快照未知时保持空，不从综合分或当前资料反推。存量升级仅根据已存在reply_content回填reply_status。默认新记录可见、未回复，保存时资料取真实远程会员和原订单快照；旧订单缺商品快照的名称/图片/规格不补当前值。

同一真实订单项唯一，逻辑删除后仍保留此购买已评价事实，不能重新投稿。相同身份及完整评价内容重试返回原ID，即使会员RPC随后不可用，也先核验已存结果；评分维度、图片顺序、匿名标记或正文变化返回409，不能改写原评价。订单归属/状态/SKU不符返回400；无会员身份401；会员或评价不存在404。网络未知结果不能假设成功或修改输入后重新投稿，原请求可重试核验。

回复只取当前管理员，保存回复内容、操作者、时间及replyStatus；相同回复重试保留首次时间。匿名仅限制公开展示，后台按权限可查看保存时昵称头像。公开响应省略会员ID、订单编号、订单ID、回复管理员ID，不复用后台响应。

## 8. 失败响应示例

统一Problem Details示例（构造，未部署实测）：
```json
{
  "type": "urn:lemnt:problem:business-rule-violation",
  "title": "业务规则校验失败",
  "status": 409,
  "detail": "该订单商品已评价，不能改写或重复评价",
  "instance": "/mall/app/comments",
  "code": "BUSINESS_RULE_VIOLATION"
}
```

HTTP字段校验失败422，缺必填spuId/type、图片超过9项或地址含空白、评分超范围等会返回VALIDATION_ERROR及errors[{pointer,message}]；维度不成对/综合分不一致由应用校验返回400。无后台权限返回403/FORBIDDEN。错误code为字符串，不能按成功R的数值code处理；有请求追踪上下文时附traceId。

## 9. 脚本、页面与验证边界

初始化：lemnt-server/lemnt-mall/sql/V16__product_management.sql；旧库扩列：sql/upgrade/product_comment_fields.sql，必须先备份、核对列是否存在，不能在新版初始化后重复执行；菜单：lemnt-server/sql/V20261029__product_comment_menu.sql，依赖既有商城根节点，不自动授权。上述脚本没有在真实数据库执行。

后台React商品评价页支持真实分页、详情、图片、回复、显隐及false筛选；小程序商品详情连接公开评价，完成订单商品连接评价创建页，并再次读取服务端订单核验归属状态和SKU。提交结果未知时冻结原输入，原请求核验后才显示保存成功；换号清理私人输入并拒绝旧响应。图片当前通过HTTPS地址填写，尚未迁移源拍照/上传SDK。

真实MySQL、Redis/Dubbo、部署网关HTTP、浏览器及真机端到端未验证；H2存储/控制器测试和前端回归不能替代这些验收。

## 后台自评（2026-10-29补充）

POST `/mall/comments`，权限 `mall:comment:create`。独立请求字段：userNickname（必填非空，80字）、userAvatar（必填HTTPS，1024字符）、skuId（正ID）、descriptionScores/benefitScores（各1-5整数）、content（非空，1000字）、picUrls（可选，最多9个HTTPS地址，每项1024字符）。响应 `R.data={id}`，失败使用统一Problem Details，HTTP字段错误422，业务参数400，SKU/SPU不存在404。

按源后台自评语义：memberId=0，不代表真实会员；orderNo、orderId、orderItemId为空。非匿名、可见、未回复；评分平均向下取整，商品名称/图片/规格为创建时真实商品快照。不会更新真实订单评价状态。每次明确创建是新记录，无内容去重；网络结果未知先查询核对，禁止自动重发。

旧库需要生成的mall `sql/upgrade/product_comment_self.sql`放开order_no空值；新版初始化已包含。新权限脚本 `lemnt-server/sql/V20261029__product_comment_self_permission.sql`只创建按钮，不授予角色。真实库均未执行。后台头像/评价图片仍填HTTPS地址，上传另行迁移。
