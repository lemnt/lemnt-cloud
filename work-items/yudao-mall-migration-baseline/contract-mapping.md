# 接口与字段适配映射

> 设计版本：2026-11-06；状态：实施计划预览配套方案，未实施。
> 依据：已确认需求/源码分析。保留当前项目R包装、元金额和安全编号契约，不按源URL机械覆盖当前路径。
> 本文完整定义首批B1/B2a的接口适配；B2b字段和B3/B4接口仍有明确门控，不冒称全量接口设计已完成。

## 1. 公共约定

- 请求以当前路径为准；DTO/Req/Res使用当前命名。以下页码为`pageNum`，不是源`pageNo`。
- Long编号通过HTTP可为安全整数或十进制字符串；页面统一转字符串，服务端校验正数，禁止浮点转换大编号。
- 金额是人民币元文本，最多两位小数；与源整数分数据对接时使用BigDecimal精确移动小数点，不使用浮点。
- 成功对象/数组：`{"code":200,"msg":"操作成功","data":...}`；分页：`{"code":200,"msg":"操作成功","rows":[...],"total":n}`。文案沿现有R，不把示例文案当作精确断言。
- 无新增对外错误码体系。沿现有异常处理器：参数绑定/Bean Validation通常422；明确业务参数错误400；缺登录401；无权限403；不存在404；内容/状态冲突409；依赖不可确认503。实施时通过实际异常映射验证，不把任意异常一律写成200或500。
- 以下公开查询权限码为“无”，不意味着所有`/app/**`接口匿名。收藏、浏览记录写操作、投稿等仍须会员令牌。

## 2. A01 商品分页

**地址**：`/app/products`；**方式**：GET；**权限码**：无（公开查询）。

源入口：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/spu/AppProductSpuController.java:L59`，确切方法参数和SQL见已确认[source-analysis F04](source-analysis.md)。目标保持当前Controller，只补查询能力。

| 参数 | 类型/位置 | 必填 | 规则 |
|---|---|---|---|
| name | string/query | 否 | 保留现有包含筛选，最长200；对应源keyword实际匹配商品名 |
| categoryId | Long/query | 否 | 正数；与categoryIds取并集 |
| categoryIds | Long[]/query | 否 | 逗号分隔或重复参数，经当前绑定验证；最多100项，去重，均为正数 |
| sortField | string/query | 否 | 仅price、salesCount、createTime；空值保持sort DESC,id DESC |
| sortAsc | boolean/query | 否 | 指定排序字段时缺省true，与源默认一致 |
| pageNum | int/query | 否 | 默认1，1..1000000 |
| pageSize | int/query | 否 | 默认20，1..100 |

选中分类集合=显式categoryId/categoryIds并集＋这些分类下**启用的直接子分类**。不递归补任意层，不自动过滤显式选择的分类。空条件代表无分类限制。由Category Gateway一次取直接子分类，避免每个分类一条SQL。

排序：price采用当前SPU商品价的既定SKU聚合口径，salesCount在B2b虚拟销量字段完成前按当前真实累计值；B2b完成后明确真实+虚拟展示值。createTime依赖SPU创建时间字段，**B2b字段未就绪时不得声称此排序已完整交付，也不能伪用ID代替创建时间**。非法排序字段400/422；SQL只通过choose固定分支选择表达式，禁止`${sortField}`插值。

请求示例：`GET /app/products?name=衣服&categoryIds=1,2&sortField=price&sortAsc=true&pageNum=1&pageSize=20`。GET无JSON body。

| 响应字段 | 类型 | 说明 |
|---|---|---|
| rows[].id | Long传输 | 商品编号 |
| rows[].name/image/categoryId/brandId | 现有类型 | 保留字段 |
| rows[].minPrice/maxPrice | string | 元金额，例9.90/12.00 |
| rows[].available | boolean | 展示库存提示，不替代建单库存校验 |
| rows[].salesCount | int | 已确认支付累计销量；不要悄悄改成真实+虚拟 |
| total | long | 与相同筛选一致的总数 |

示例：`{"code":200,"msg":"操作成功","rows":[{"id":"1001","name":"衣服","minPrice":"9.90","maxPrice":"12.00","available":true,"salesCount":3}],"total":1}`（省略未变字段）。

B2a不改商品价格正数/有效SKU存在等现行可见性；源可见性差异必须由B2b一并解决约束、详情和购买校验后验收，不能只改一条查询SQL。

错误：参数400/422；公开查询不要求登录；依赖不可用按现有系统异常处理，不伪造空列表成功。

## 3. A02 商品详情

**地址**：`/app/products/{id}`；**方式**：GET；**权限码**：无。

| 参数 | 类型/位置 | 必填 | 规则 |
|---|---|---|---|
| id | Long/path | 是 | 正数商品编号 |

请求：`GET /app/products/1001`。无body。

B2a保持当前成功null的兼容行为（源不存在/下架抛错差异在B2b显式迁移），补已持久化数据输出，不新增DB字段。

| 新增/保留响应字段 | 类型 | 来源 |
|---|---|---|
| data.id/name/categoryId/brandId/description/image | 现有类型 | 保留 |
| data.skus | array | 保留 |
| data.salesCount | int | 保留支付累计口径 |
| data.keyword/introduction | string或null | 现有SPU字段，不从当前名称补造历史值 |
| data.sliderPicUrls | string[]或null | 现有持久化快照，未知保持null |
| data.deliveryTypes/deliveryTemplateId | array/Long或null | 现有配送配置 |
| data.skus[].weight/volume | 精确数值边界 | 现有SKU重量kg、体积m³；不能把null转为0 |

示例：`{"code":200,"msg":"操作成功","data":{"id":"1001","name":"衣服","introduction":"棉质上衣","sliderPicUrls":["https://example.test/a.png"],"salesCount":3,"skus":[{"id":"2001","price":"9.90","stock":10,"weight":0.2,"volume":null}]}}`（省略其他现有字段）。

错误：id非法400/422；B2a不存在仍`data:null`，明确不是最终源语义验收；B2b改变为404/业务不可见错误时，先更新两端和测试，不静默切换。

## 4. A03 启用分类列表（补ids）

**地址**：`/app/categories`；**方式**：GET；**权限码**：无。

| 参数 | 类型/位置 | 必填 | 规则 |
|---|---|---|---|
| ids | Long[]/query | 否 | 最多100项，正数，去重；未给则全量启用分类；显式空列表需绑定层区分，不悄悄变成任意查询 |

请求：`GET /app/categories?ids=1,3`；全量时`GET /app/categories`。无body。

返回按sort ASC,id ASC稳定排序，只过滤自身启用状态，与源分类口径一致，不以父分类停用隐藏启用子类。

| 响应 | 类型 | 说明 |
|---|---|---|
| data | array | 非分页启用分类集合 |
| data[].id/parentId | Long/null | 原编号/父编号 |
| data[].name/picUrl | string/null | 原展示字段 |

示例：`{"code":200,"msg":"操作成功","data":[{"id":"3","parentId":"1","name":"上衣","picUrl":null}]}`。

错误：ids非法400/422；不存在/停用ID不返回；不存在新权限码；不覆盖后台分类列表的权限。

源参考：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/category/AppCategoryController.java:L35-L54`；当前`ProductCategoryGatewayImpl.java:L73-L76`（完整路径见[source-analysis](source-analysis.md)）。

## 5. A04 商品公开评价分页（修正调用地址）

**地址**：`/app/comments`；**方式**：GET；**权限码**：无。目标product；只改小程序读地址和相关鉴权，不改评价分页业务。

| 参数 | 类型/位置 | 必填 | 规则 |
|---|---|---|---|
| spuId | Long/query | 是 | 正数 |
| type | int/query | 是 | 0全部/1好评/2中评/3差评；小程序默认0 |
| pageNum/pageSize | int/query | 否 | 既有默认及上限 |

请求：`GET /app/comments?spuId=1001&type=0&pageNum=1&pageSize=20`。

响应：rows/total；rows元素沿现有公开Res（匿名身份脱敏、图片、评分、正文、商家回复）。不新增后台字段。示例：`{"code":200,"msg":"操作成功","rows":[],"total":0}`。

错误：缺spuId/type或非法范围422；匿名GET不得要求管理员令牌；请求中userId/visible不能扩大查询范围。

## 6. A05 会员投稿（保留订单归属）

**地址**：`/mall/app/comments`；**方式**：POST；**权限码**：无后台权限码，但须会员登录；目标order。

| 参数 | 类型/位置 | 必填 | 规则 |
|---|---|---|---|
| orderNo | string/body | 是 | 本人完成订单号，最长40 |
| skuId | Long/body | 是 | 真实订单SKU |
| orderItemId | Long/body | 新页面须提供 | 同SKU多行必须精确定位；兼容规则沿当前订单service |
| score | int/body | 否 | 兼容单分1..5；与双维度校验沿现有逻辑 |
| descriptionScores/benefitScores | int/body | 成对 | 1..5 |
| content | string/body | 是 | 1..1000字 |
| anonymous | boolean/body | 否 | 默认false |
| picUrls | string[]/body | 否 | 最多9张HTTPS图片，按既有规则校验 |

请求示例：`{"orderNo":"LOCAL-1","skuId":"2001","orderItemId":"3001","descriptionScores":5,"benefitScores":4,"content":"真实评价","anonymous":false,"picUrls":[]}`。

响应字段`data.id`为已创建评价编号。示例：`{"code":200,"msg":"操作成功","data":{"id":"4001"}}`。

错误：未登录401；他人订单/项404或既有业务映射；未完成/非法输入400/422；同项不一致投稿409；远端结果未确认503。昵称/头像/会员ID来自可信上下文，不接收body伪造值。用户重复投稿按原订单项幂等，不新增另一张投稿消息表。

## 7. A06 收藏/足迹等会员接口（只修身份分类）

路径与请求/响应本次无变更。网关对现存`/app/favorites`、其count/exists/删除子路径，以及`/app/browse-historys`及clear的真实路由按会员身份校验。匿名401、管理员令牌不能当会员令牌；准确路由由当前端点清单驱动回归。不得简单放行`/app/**`所有方法。

## 8. B2b数据与展示映射（需字段方案门控）

| 源字段 | 目标方案 | 未知/兼容规则 |
|---|---|---|
| SPU.specType | specType boolean | 新资料显式填写；历史缺失保留null，不按SKU个数补造 |
| SKU.marketPrice/costPrice | BigDecimal元；HTTP元文本 | null表示历史未记录；不能用售价替代成本/划线价 |
| SPU.marketPrice/costPrice | 按源最小SKU聚合口径输出 | 明确null传播；不无理由新增另一份可手改SPU价格 |
| SKU.barCode | barCode字符串 | 与当前业务code不同，不覆盖code |
| SPU.giveIntegral | giveIntegral非负整数 | 与订单赠积分快照/会员规则联动后才算完成 |
| SPU.subCommissionType | subCommissionType boolean | 与SKU一级/二级佣金及订单分佣配置联动 |
| SKU.first/secondBrokeragePrice | 元金额文本 | 不在前端浮点计算 |
| SPU.virtualSalesCount | 虚拟展示量 | 保持salesCount真实支付累计字段语义；另输出displaySalesCount=真实+虚拟，未知值不自动视作0 |
| SPU.browseCount | 浏览量 | 详情浏览按源语义增量；归属当前商品域，不新建门店模型 |
| SKU.salesCount | 源有字段，当前尚无对应 | 不在B2a悄悄新增；与SKU销量写入、退款口径一并在B2b确认 |
| SPU.createTime | 创建时间 | 新资料记录；历史未知不可用当前时间或ID伪造 |
| picture/properties命名 | 继续image/specs并映射 | 能力对齐，不机械重命名全部公开字段 |

源`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/spu/vo/ProductSkuSaveReqVO.java:L19-L22`以@Min(0)允许零售价。恢复该能力时，售价约束、管理端输入、展示、结算可售及零金额订单处理必须在B2b同批对齐；详情异常和浏览副作用也需源Service联动验证，不得只解除一条SQL约束便宣称零元链路已可用。

## 9. 跨服务协议隔离

外部事件的JSON字段、eventNo、businessKey、schemaVersion保持不变。每个消费上下文声明自己的`domain/event/gateway/InboundEventGateway`，输入为原payload字符串，输出`domain/event/model/InboundEventFacts`内的明确record（只放当前上下文需要的事件类型及字段）。infra实现用外部Client类型解码并映射；application只能使用本域record，不导入外部Client。共用CommerceEventEnvelope属于公共契约，不借此把所有业务DTO塞入common。

映射必须保留金额、long积分、业务号、订单项、活动赠券数量等真实消费字段，不用`Map<String,Object>`或Object返回值。缺字段/未知版本抛异常，不能把错误事件登记为成功消费。先完成类型映射测试及事件回归，再移除6处application生产依赖；本服务Client提供者的依赖不删除。

## 10. 本域入站事实契约（B1b）

每个上下文只定义它实际消费的嵌套record，不生成全部事件的万能对象。所有ID保持Long、金额BigDecimal、积分long，集合项使用本域类型；外部JSON解析和字段映射只出现在infra。

| 本域record形态 | 字段（保持现有载荷类型） |
|---|---|
| Paid | orderNo:String、memberId:Long、amount:BigDecimal、usePoint/givePoint:long、giveCouponTemplateCounts:Map<Long,Integer>、items:List<StockLine> |
| Closed | orderNo:String、memberId:Long、items:List<StockLine> |
| AfterSaleCompleted | orderNo/afterSaleNo:String、memberId/orderItemId/skuId:Long、quantity:int、amount:BigDecimal、returnUsePoint/deductGivePoint:long、ruleVersion:int、fullyRefunded:boolean |
| Completed / Shipped | orderNo:String、memberId:Long |
| StockPreReserved | reservationNo:String、skuId:Long、quantity:int |
| PaymentSucceeded | paymentNo/orderNo/channel/channelTradeNo:String、memberId:Long、amount:BigDecimal |
| RefundSucceeded | refundNo/afterSaleNo/paymentNo/orderNo/channel/channelRefundNo:String、memberId:Long、amount:BigDecimal |
| StockLine | skuId:Long、quantity:int |

端口方法按上下文收敛：promotion为paidFacts/closedFacts/afterSaleFacts/completedFacts/stockPreReservedFacts；order为paymentSucceededFacts/refundSucceededFacts；pay仅closedFacts；member为paidFacts/closedFacts/afterSaleFacts/shippedFacts。各方法接受原payload:String，返回对应`InboundEventFacts.Xxx`，不暴露外部Client对象。

必须测试缺字段、null集合、非法件数、大积分、重复/乱序载荷以及原业务校验。映射层不把未知/null擅自变成空集合或零，不能因字段转换而减弱原handler拒绝条件；事件信封版本/类型、幂等键和冻结内容校验继续保留。

## 11. 契约验收与更新

- GET/POST评价URL由mock request分别断言；不能只验证POST。
- 路由predicate、鉴权filter、Controller绑定、权限码、前端URL和解码联合验证。
- 新增ids或查询参数不增加URL条目；新增实际方法/路径时先更新能力映射及端点证据，再运行网关契约测试。
- 既有端点契约文件是当前项目契约证据，不是源项目全量能力清单。
