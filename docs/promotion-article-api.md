# 商城文章与分类接口

2026-10-29。参考本地芋道商城文章模块，适配当前R响应、域和认证边界。以下为按当前代码核对的构造示例，未做部署HTTP验收；编号按当前Jackson配置输出字符串，日期为本地ISO时间，不虚构时区。可空字段按NON_NULL配置省略。

## 通用参数与响应约定

分页参数：pageNum（Query整数，可选，默认1，1至1000000）、pageSize（Query整数，可选，默认20，1至100）。分页使用顶层rows/total，不引入自定义分页对象。业务成功code=200；未知业务记录404、分类有文章不可删除409、业务参数非法400；Bean Validation非法HTTP参数422；无权限403、登录失效401由统一认证/权限处理。App指定公开路径不要求登录。

文章公开查询沿源行为，不按文章status过滤；分类App列表仅启用0、按sort升序，后台分类sort降序、文章分页id降序。标题/作者精确匹配，分类name模糊匹配；false/0不能当作未传。

## 1. 分页分类

接口地址：`/mall/promotion/article-categories`；请求方式：`GET`；权限：`mall:article-category:list`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|name|string|否|Query|无|分类名称，非空，最多128字符|
|status|integer|否|Query|无|0启用，1禁用|
|createFrom|string|否|Query|无|创建起始时间，ISO本地时间，含边界|
|createTo|string|否|Query|无|创建结束时间，ISO本地时间，含边界|
|pageNum|integer|否|Query|1|页码，1至1000000|
|pageSize|integer|否|Query|20|每页，1至100|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|rows|array|当前分页行，空页为[]|
|total|integer|符合条件总数|
|rows[].id|string|编号|
|rows[].name|string|分类名称，非空，最多128字符|
|rows[].status|integer|0启用，1禁用|
|rows[].sort|integer|非负排序|
|rows[].createTime|string|创建时间，ISO本地时间|
|rows[].picUrl|string|图片地址，最多1024字符；分类可空|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/promotion/article-categories?pageNum=1&pageSize=20' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "rows": [
    {
      "id": "12",
      "name": "商城公告",
      "status": 0,
      "sort": 0,
      "createTime": "2026-10-29T10:00:00"
    }
  ],
  "total": 1
}
```

## 2. 读取分类

接口地址：`/mall/promotion/article-categories/{id}`；请求方式：`GET`；权限：`mall:article-category:query`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.id|string|编号|
|data.name|string|分类名称，非空，最多128字符|
|data.status|integer|0启用，1禁用|
|data.sort|integer|非负排序|
|data.createTime|string|创建时间，ISO本地时间|
|data.picUrl|string|图片地址，最多1024字符；分类可空|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/promotion/article-categories/12' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": "12",
    "name": "商城公告",
    "status": 0,
    "sort": 0,
    "createTime": "2026-10-29T10:00:00"
  }
}
```

## 3. 新增分类

接口地址：`/mall/promotion/article-categories`；请求方式：`POST`；权限：`mall:article-category:create`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|name|string|是|Body|无|分类名称，非空，最多128字符|
|picUrl|string|否|Body|无|图片地址，最多1024字符；分类可空|
|status|integer|是|Body|无|0启用，1禁用|
|sort|integer|是|Body|无|非负排序|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.id|string|编号|

请求示例：
```sh
curl -X POST 'https://<API_HOST>/mall/promotion/article-categories' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"name": "商城公告", "status": 0, "sort": 0}'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": "12"
  }
}
```

## 4. 修改分类

接口地址：`/mall/promotion/article-categories/{id}`；请求方式：`PUT`；权限：`mall:article-category:update`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|
|name|string|是|Body|无|分类名称，非空，最多128字符|
|picUrl|string|否|Body|无|图片地址，最多1024字符；分类可空|
|status|integer|是|Body|无|0启用，1禁用|
|sort|integer|是|Body|无|非负排序|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.success|boolean|操作确认成功true|

请求示例：
```sh
curl -X PUT 'https://<API_HOST>/mall/promotion/article-categories/12' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"name": "商城公告", "status": 0, "sort": 0}'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 5. 删除分类

接口地址：`/mall/promotion/article-categories/{id}`；请求方式：`DELETE`；权限：`mall:article-category:delete`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.success|boolean|操作确认成功true|

请求示例：
```sh
curl -X DELETE 'https://<API_HOST>/mall/promotion/article-categories/12' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 6. 分页文章

接口地址：`/mall/promotion/articles`；请求方式：`GET`；权限：`mall:article:list`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|categoryId|string|否|Query|无|分类编号，正整数，必须存在|
|spuId|string|否|Query|无|商品编号，非负，0表示未关联|
|title|string|否|Query|无|标题，非空，最多255字符|
|author|string|否|Query|无|作者，可空，最多128字符|
|status|integer|否|Query|无|0启用，1禁用|
|recommendHot|boolean|否|Query|无|是否热门，false有效|
|recommendBanner|boolean|否|Query|无|是否轮播，false有效|
|createFrom|string|否|Query|无|创建起始时间，ISO本地时间，含边界|
|createTo|string|否|Query|无|创建结束时间，ISO本地时间，含边界|
|pageNum|integer|否|Query|1|页码，1至1000000|
|pageSize|integer|否|Query|20|每页，1至100|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|rows|array|当前分页行，空页为[]|
|total|integer|符合条件总数|
|rows[].id|string|编号|
|rows[].categoryId|string|分类编号，正整数，必须存在|
|rows[].spuId|string|商品编号，非负，0表示未关联|
|rows[].title|string|标题，非空，最多255字符|
|rows[].picUrl|string|图片地址，最多1024字符；分类可空|
|rows[].sort|integer|非负排序|
|rows[].status|integer|0启用，1禁用|
|rows[].recommendHot|boolean|是否热门，false有效|
|rows[].recommendBanner|boolean|是否轮播，false有效|
|rows[].content|string|HTML正文，非空，最多1000000字符|
|rows[].browseCount|integer|实际浏览次数，新增0，不接受写入资料请求修改|
|rows[].createTime|string|创建时间，ISO本地时间|
|rows[].author|string|作者，可空，最多128字符|
|rows[].introduction|string|简介，可空，最多2000字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/promotion/articles?pageNum=1&pageSize=20' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "rows": [
    {
      "id": "18",
      "categoryId": "12",
      "spuId": "0",
      "title": "商城介绍",
      "picUrl": "https://example.test/cover.png",
      "sort": 0,
      "status": 0,
      "recommendHot": false,
      "recommendBanner": false,
      "content": "<p>商城介绍正文</p>",
      "browseCount": 0,
      "createTime": "2026-10-29T10:00:00"
    }
  ],
  "total": 1
}
```

## 7. 读取文章

接口地址：`/mall/promotion/articles/{id}`；请求方式：`GET`；权限：`mall:article:query`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.id|string|编号|
|data.categoryId|string|分类编号，正整数，必须存在|
|data.spuId|string|商品编号，非负，0表示未关联|
|data.title|string|标题，非空，最多255字符|
|data.picUrl|string|图片地址，最多1024字符；分类可空|
|data.sort|integer|非负排序|
|data.status|integer|0启用，1禁用|
|data.recommendHot|boolean|是否热门，false有效|
|data.recommendBanner|boolean|是否轮播，false有效|
|data.content|string|HTML正文，非空，最多1000000字符|
|data.browseCount|integer|实际浏览次数，新增0，不接受写入资料请求修改|
|data.createTime|string|创建时间，ISO本地时间|
|data.author|string|作者，可空，最多128字符|
|data.introduction|string|简介，可空，最多2000字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/promotion/articles/18' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": "18",
    "categoryId": "12",
    "spuId": "0",
    "title": "商城介绍",
    "picUrl": "https://example.test/cover.png",
    "sort": 0,
    "status": 0,
    "recommendHot": false,
    "recommendBanner": false,
    "content": "<p>商城介绍正文</p>",
    "browseCount": 0,
    "createTime": "2026-10-29T10:00:00"
  }
}
```

## 8. 新增文章

接口地址：`/mall/promotion/articles`；请求方式：`POST`；权限：`mall:article:create`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|categoryId|string|是|Body|无|分类编号，正整数，必须存在|
|spuId|string|是|Body|无|商品编号，非负，0表示未关联|
|title|string|是|Body|无|标题，非空，最多255字符|
|author|string|否|Body|无|作者，可空，最多128字符|
|picUrl|string|是|Body|无|图片地址，最多1024字符；分类可空|
|introduction|string|否|Body|无|简介，可空，最多2000字符|
|sort|integer|是|Body|无|非负排序|
|status|integer|是|Body|无|0启用，1禁用|
|recommendHot|boolean|是|Body|无|是否热门，false有效|
|recommendBanner|boolean|是|Body|无|是否轮播，false有效|
|content|string|是|Body|无|HTML正文，非空，最多1000000字符|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.id|string|编号|

请求示例：
```sh
curl -X POST 'https://<API_HOST>/mall/promotion/articles' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"categoryId": "12", "spuId": "0", "title": "商城介绍", "picUrl": "https://example.test/cover.png", "sort": 0, "status": 0, "recommendHot": false, "recommendBanner": false, "content": "<p>商城介绍正文</p>"}'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": "18"
  }
}
```

## 9. 修改文章

接口地址：`/mall/promotion/articles/{id}`；请求方式：`PUT`；权限：`mall:article:update`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|
|categoryId|string|是|Body|无|分类编号，正整数，必须存在|
|spuId|string|是|Body|无|商品编号，非负，0表示未关联|
|title|string|是|Body|无|标题，非空，最多255字符|
|author|string|否|Body|无|作者，可空，最多128字符|
|picUrl|string|是|Body|无|图片地址，最多1024字符；分类可空|
|introduction|string|否|Body|无|简介，可空，最多2000字符|
|sort|integer|是|Body|无|非负排序|
|status|integer|是|Body|无|0启用，1禁用|
|recommendHot|boolean|是|Body|无|是否热门，false有效|
|recommendBanner|boolean|是|Body|无|是否轮播，false有效|
|content|string|是|Body|无|HTML正文，非空，最多1000000字符|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.success|boolean|操作确认成功true|

请求示例：
```sh
curl -X PUT 'https://<API_HOST>/mall/promotion/articles/18' -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' --data '{"categoryId": "12", "spuId": "0", "title": "商城介绍", "picUrl": "https://example.test/cover.png", "sort": 0, "status": 0, "recommendHot": false, "recommendBanner": false, "content": "<p>商城介绍正文</p>"}'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 10. 删除文章

接口地址：`/mall/promotion/articles/{id}`；请求方式：`DELETE`；权限：`mall:article:delete`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|
|id|string|是|Path|无|编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.success|boolean|操作确认成功true|

请求示例：
```sh
curl -X DELETE 'https://<API_HOST>/mall/promotion/articles/18' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 11. 后台启用分类选项

接口地址：`/mall/promotion/article-categories/enabled`；请求方式：`GET`；权限：`mall:article-category:list`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|Authorization|string|是|Header|无|Bearer <ADMIN_TOKEN>|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|array|当前独立响应对象或列表|
|data[].id|string|编号|
|data[].name|string|分类名称，非空，最多128字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/promotion/article-categories/enabled' -H 'Authorization: Bearer <ADMIN_TOKEN>'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": [
    {
      "id": "12",
      "name": "商城公告"
    }
  ]
}
```

## 12. App启用分类

接口地址：`/mall/app/article-categories`；请求方式：`GET`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|无|-|-|-|-|无请求参数|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|array|当前独立响应对象或列表|
|data[].id|string|编号|
|data[].name|string|分类名称，非空，最多128字符|
|data[].picUrl|string|图片地址，最多1024字符；分类可空|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/app/article-categories'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": [
    {
      "id": "12",
      "name": "商城公告"
    }
  ]
}
```

## 13. App文章分页

接口地址：`/mall/app/articles`；请求方式：`GET`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|categoryId|string|否|Query|无|分类编号，正整数，必须存在|
|pageNum|integer|否|Query|1|页码，1至1000000|
|pageSize|integer|否|Query|20|每页，1至100|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|rows|array|当前分页行，空页为[]|
|total|integer|符合条件总数|
|rows[].id|string|编号|
|rows[].categoryId|string|分类编号，正整数，必须存在|
|rows[].spuId|string|商品编号，非负，0表示未关联|
|rows[].title|string|标题，非空，最多255字符|
|rows[].picUrl|string|图片地址，最多1024字符；分类可空|
|rows[].content|string|HTML正文，非空，最多1000000字符|
|rows[].browseCount|integer|实际浏览次数，新增0，不接受写入资料请求修改|
|rows[].createTime|string|创建时间，ISO本地时间|
|rows[].author|string|作者，可空，最多128字符|
|rows[].introduction|string|简介，可空，最多2000字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/app/articles?pageNum=1&pageSize=20'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "rows": [
    {
      "id": "18",
      "categoryId": "12",
      "spuId": "0",
      "title": "商城介绍",
      "picUrl": "https://example.test/cover.png",
      "content": "<p>商城介绍正文</p>",
      "browseCount": 0,
      "createTime": "2026-10-29T10:00:00"
    }
  ],
  "total": 1
}
```

## 14. App推荐文章列表

接口地址：`/mall/app/articles/recommended`；请求方式：`GET`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|recommendHot|boolean|否|Query|无|是否热门，false有效|
|recommendBanner|boolean|否|Query|无|是否轮播，false有效|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|array|当前独立响应对象或列表|
|data[].id|string|编号|
|data[].categoryId|string|分类编号，正整数，必须存在|
|data[].spuId|string|商品编号，非负，0表示未关联|
|data[].title|string|标题，非空，最多255字符|
|data[].picUrl|string|图片地址，最多1024字符；分类可空|
|data[].content|string|HTML正文，非空，最多1000000字符|
|data[].browseCount|integer|实际浏览次数，新增0，不接受写入资料请求修改|
|data[].createTime|string|创建时间，ISO本地时间|
|data[].author|string|作者，可空，最多128字符|
|data[].introduction|string|简介，可空，最多2000字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/app/articles/recommended?recommendHot=false&recommendBanner=false'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": [
    {
      "id": "18",
      "categoryId": "12",
      "spuId": "0",
      "title": "商城介绍",
      "picUrl": "https://example.test/cover.png",
      "content": "<p>商城介绍正文</p>",
      "browseCount": 0,
      "createTime": "2026-10-29T10:00:00"
    }
  ]
}
```

## 15. App文章详情

接口地址：`/mall/app/articles/detail`；请求方式：`GET`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|id|string|二选一|Query|无|编号|
|title|string|二选一|Query|无|标题，非空，最多255字符|

id存在时优先；否则按title精确匹配id最大的有效文章。两者均缺失报400；无匹配成功返回空data（NON_NULL序列化时省略data）。GET不增加浏览量。

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.id|string|编号|
|data.categoryId|string|分类编号，正整数，必须存在|
|data.spuId|string|商品编号，非负，0表示未关联|
|data.title|string|标题，非空，最多255字符|
|data.picUrl|string|图片地址，最多1024字符；分类可空|
|data.content|string|HTML正文，非空，最多1000000字符|
|data.browseCount|integer|实际浏览次数，新增0，不接受写入资料请求修改|
|data.createTime|string|创建时间，ISO本地时间|
|data.author|string|作者，可空，最多128字符|
|data.introduction|string|简介，可空，最多2000字符|

请求示例：
```sh
curl -X GET 'https://<API_HOST>/mall/app/articles/detail?id=18'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "id": "18",
    "categoryId": "12",
    "spuId": "0",
    "title": "商城介绍",
    "picUrl": "https://example.test/cover.png",
    "content": "<p>商城介绍正文</p>",
    "browseCount": 0,
    "createTime": "2026-10-29T10:00:00"
  }
}
```

## 16. 增加浏览次数

接口地址：`/mall/app/articles/{id}/browse-count`；请求方式：`PUT`；权限：`公开`。

|参数|类型|必填|位置|默认值|说明|
|---|---|---|---|---|---|
|id|string|是|Path|无|编号|

|响应字段|类型|说明|
|---|---|---|
|code|integer|业务状态码，成功200|
|msg|string，可空|统一错误消息，成功通常省略|
|data|object|当前独立响应对象或列表|
|data.success|boolean|操作确认成功true|

请求示例：
```sh
curl -X PUT 'https://<API_HOST>/mall/app/articles/18/browse-count'
```

成功响应示例（构造，未部署实测）：
```json
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

## 存储、交互与未验证项

文章及分类分别对应 mall_promotion_article、mall_promotion_article_category，采用MyBatis Plus逻辑删除。分类删除与文章新增/修改锁定分类行，事务结束释放；无额外请求表/缓存。更新资料只更新白名单字段，不回写browseCount/createTime。浏览量通过独立PUT执行原子+1，非幂等操作；超时不能自动重试，正文可继续展示并提示计数未确认。当前显示次数来自详情读取时刻，不由客户端假设加一。

后台图片通过既有 /system/files/upload 上传，要求已登录；富文本采用现有Quill及DOMPurify。小程序使用原生rich-text，支持id/title详情入口，无法使用任意WebView或执行正文脚本。原生页面并未逐像素复刻源Vue/uni-app组件。

参数错误示例：
```json
{"code": 422, "msg": "请求参数错误"}
```

错误msg由统一异常处理产生，文本随校验项变化。无权限示例：
```json
{"code": 403, "msg": "没有权限"}
```

新DDL和菜单权限脚本仅生成，未对真实数据库执行；菜单种子需已有商城根节点，不自动授权角色。H2、API合同及前端单元回归不替代真实MySQL、部署网关HTTP、浏览器和设备端到端验收。
