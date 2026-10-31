# 商品收藏与浏览足迹接口

2026-10-29。参考本地芋道商品收藏、浏览记录服务及 App 收藏/足迹页面，按当前服务分层和认证边界迁移。所有端点有独立请求、响应类型；分页复用项目 `R.page`，返回顶层 `rows`、`total`，不新增分页对象。

## 会员接口

以下路径均需要网关认证；会员身份取 `SecurityUtils`，不接受客户端指定会员编号。

|方法|路径|入参|成功 data|
|---|---|---|---|
|POST|`/mall/app/favorites`|JSON `spuId`，正整数|`{id}` 收藏记录编号|
|DELETE|`/mall/app/favorites/{spuId}`|路径商品编号，正整数|`{success:true}`|
|GET|`/mall/app/favorites`|`pageNum` 默认1、`pageSize` 默认20，最大100|分页，见下表|
|GET|`/mall/app/favorites/exists`|Query `spuId`，正整数|`{favorite:false}` 或 true|
|GET|`/mall/app/favorites/count`|无|`{count:0}` 或实际数量|
|POST|`/mall/app/browse-history`|JSON `spuId`，正整数|`{success:true}`|
|GET|`/mall/app/browse-history`|`pageNum`、`pageSize` 同上|分页，仅本人未隐藏记录|
|DELETE|`/mall/app/browse-history`|JSON `spuIds`，1至100个正整数|`{success:true}`|
|DELETE|`/mall/app/browse-history/clear`|无|`{success:true}`|

分页页码最大1000000。收藏分页行：

|字段|含义|
|---|---|
|id|收藏记录编号|
|spuId|商品编号|
|spuName|当前商品名称，商品不存在时 null|
|picUrl|当前商品图片，缺失时 null|
|price|当前 SKU 最低价，元；缺失时 null|

足迹分页行使用独立响应类，含上表字段及 `createTime`（最近浏览时间）。App 不返回会员编号或后台隐藏标记。编号由项目序列化配置处理，客户端按字符串保存避免精度丢失；价格由项目金额序列化规则处理，客户端显示元字符串，不将源项目的“分”直接复用。

写入、状态、数量响应使用 `R.ok`，如 `{"code":200,"msg":null,"data":{"favorite":false}}`；分页使用 `{"code":200,"msg":null,"rows":[],"total":0}`。`false`、0 与空列表均为有效结果，示例为字段节选，空字段是否输出遵循统一序列化配置。

收藏重复添加报业务409；取消不存在的收藏报404。服务内无会员身份报401；非法 HTTP 入参由统一异常处理返回422，业务层非法参数返回400。使用统一错误响应，不新增专属异常处理器。

## 后台接口

|方法|路径|权限|Query|
|---|---|---|---|
|GET|`/mall/product-favorites`|`mall:product-favorite:list`|可选 `userId`、`spuId`，分页参数|
|GET|`/mall/product-browse-history`|`mall:product-history:list`|可选 `userId`、`spuId`、`userDeleted`，分页参数|

收藏行除 App 商品字段外包含 `userId`、`createTime`。足迹行另含 `userDeleted`。`userDeleted=false` 必须作为有效筛选传递；不传表示查询当前逻辑有效记录的两种隐藏状态。商品详情抽屉内提供按权限显示的只读表格，无新增后台主菜单或自动角色授权。

## 行为与存储

收藏按会员及商品判重。足迹每会员每商品保留最新有效记录，重新浏览替换旧行并恢复可见；最多100条有效记录，包括会员隐藏记录。移除、清空足迹设置 `user_deleted`，后台仍可查看；重排与额度淘汰通过 MyBatis Plus 逻辑删除保留旧行。收藏取消同样逻辑删除。并发写入复用基础锁，会员锁保持到事务结束，裁剪、插入失败整体回滚。

公开商品详情在现有网关会清除身份，因此登录会员在成功加载详情后调用独立受保护 POST 记录足迹；匿名不记录，登录返回已有详情时补记。mall 不解析 Token，不引入会话服务。收藏写入结果不确定时重新读取状态，页面不假设成功。

小程序收藏、足迹支持当前页全选、批量移除和分页；收藏批量按源页面顺序调用单条取消，部分失败停止并重新查询实际结果，不承诺整批事务。足迹批量使用单次受保护请求。换号时丢弃旧响应，收藏批量继续下一项前检查身份。网络失败显示错误并重新查询；查询失败清空旧列表。

两张业务表：`mall_product_favorite`、`mall_product_browse_history`。分别使用独立 DO、BaseMapper、Gateway；简单查询使用 Lambda。生成脚本位于 `lemnt-server/lemnt-mall/sql/V17__product_interaction.sql`；权限脚本位于 `lemnt-server/sql/V20261029__product_interaction_permissions.sql`。均未在真实数据库执行。

当前页展示使用现有商品事实；未添加参考响应中当前模型尚不具备的销量、统计库存等字段。前端采用现有 React 和原生小程序结构，未宣称与 Vue/uni-app 样式逐像素一致。真实 MySQL、Redis、部署网关、浏览器和真机联调未执行。
