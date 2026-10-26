# 促销 T3：权威事实 RPC 与管理接口

需求ID：yudao-member-mall-trade-migration；promotion-v1/T3；2026-10-26（Asia/Shanghai）；责任：code-software-engineer。输入35fc8ec、T1/T2和promotion-v1设计。Java17/Boot4.1/Dubbo/Nacos与原六层保留，无新模块或运行依赖。

**已实现六个后台管理入口和独立商城事实RPC；尚未接入V2购买链路或页面。** 管理权限定义脚本只作为文件交付，未执行、未授予角色。V9仍须在目标环境审核/验证后部署；本次没有main修改、merge、部署或线上SQL。

## 模块与契约

- mall-client新增PromotionCatalogClient及独立Serializable Bo/DTO，不改ProductCatalogClient。mall-domain/promotion定义纯事实及批量校验，mall-infrastructure/promotion读取实际SKU/SPU/分类/父分类/品牌，mall-application/dubbo提供RPC实现。
- trade-domain/promotion新增PromotionCatalogFactsGateway及领域事实；trade-infrastructure封装Dubbo、缺失方法/超时/协议/数据校验。application/promotion的Cmd/Qry、Executor、DTO和查询事务复用T2管理服务；adapter/web/promotion提供Req/Res/Converter、局部严格JSON、认证和错误处理。
- 价格由商城BigDecimal元精确转long分，非整分/溢出失败；只返回当前可售SKU及其权威SPU。库存0可返回，缺失/下架SKU保持缺失，后续购买调用者必须拒绝缺失项及不足库存。旧无SPU商品不能猜测映射。
- factsHash包含协议、SKU/SPU、价格、编码/名称及可售语义，UTF-8字段长度前缀；不包含瞬时stock。库存变化不改变hash，价格/SPU/名称/编码变化改变hash；分类/品牌禁用使事实缺失。
- 范围校验仅判断静态ID存在，不保证未来可售或库存；草稿允许指向暂未上架商品。ALL不调用RPC。create/save/publish首次执行验证范围；成功幂等回放先返回原回执，不依赖RPC或当前revision；权限检查仍先于回放。

## 新Dubbo接口（内部服务，非公开HTTP）

接口类：com.lemnt.mall.client.PromotionCatalogClient；provider位于application/dubbo；consumer check=false、timeout3000ms、retries0。旧provider缺接口/方法或故障返回CATALOG_FACTS_UNAVAILABLE，不回退客户端价格或旧无SPU事实。需先部署支持新RPC的mall provider。

| 方法 | 请求字段 | 响应字段与语义 |
| --- | --- | --- |
| findPricingFactsBatch | PricingFactsBatchBo.skuIds:List<Long>，1..100、正数、去重 | List<ProductPricingFactsDTO>；只返回当前可售项，排序固定 |
| validateScope | PromotionScopeValidationBo.scopeType:SKU_IDS/SPU_IDS；ids:List<Long>，1..1000、正数、去重 | PromotionScopeValidationDTO.missingIds:List<Long>，空表示本次读取存在 |

| ProductPricingFactsDTO字段 | 类型 | 语义 |
| --- | --- | --- |
| factsProtocol | String | 固定MALL_PRICING_V1 |
| skuId/spuId | Long | 正整数权威关系 |
| productCode/name | String | 服务端编码及名称，内部RPC字段 |
| unitPriceFen | long | 正整数分，精确转换 |
| availableStock | int | 非负可用库存 |
| factsHash | String | 64位小写SHA256，排除库存 |

consumer拒绝null响应/记录、未知协议、重复/请求外ID、缺失SPU、非法价格/库存/名称/编码/hash；范围响应的null/重复/请求外missingIds也拒绝。正常missingIds非空是参数错误，RPC失败或损坏响应为503。RPC校验与交易库写入不是分布式事务，报价和提单仍须重新查询权威事实。

RPC调用构造示例（未部署实测，JSON仅为阅读示意；实际Dubbo使用Bo/DTO及Java long，不是新HTTP路由）：

```java
client.findPricingFactsBatch(new PricingFactsBatchBo(List.of(101L)));
client.validateScope(new PromotionScopeValidationBo("SPU_IDS", List.of(11L, 12L)));
```

findPricingFactsBatch结果示意：

```json
[
  {
    "factsProtocol": "MALL_PRICING_V1",
    "skuId": "101",
    "spuId": "11",
    "productCode": "SKU101",
    "name": "商品101",
    "unitPriceFen": "1001",
    "availableStock": 0,
    "factsHash": "09f6c8145368fa990f2e266b29e197f391fd5712a620e52054def3981e82d53b"
  }
]
```

validateScope结果构造示例（请求中12不存在）：

```json
{"missingIds":["12"]}
```

## 管理HTTP与权限

完整六列请求表、嵌套响应表和curl/JSON构造示例见[api.md](api.md)第1..6节；对应JSON Schema已同步修订号0。示例不是生产HTTP抓包；本地MockMvc实际运行真实Controller/转换/权限与SQL服务。

| 方法/地址 | 权限 | 实际操作 |
| --- | --- | --- |
| POST /trade/admin/promotions | trade:promotion:create | 创建DRAFT，revision0 |
| PUT /trade/admin/promotions/{id}/draft | trade:promotion:update | 保存完整草稿，expectedRevision与requestNo |
| POST /trade/admin/promotions/{id}/publish | trade:promotion:publish | 复核范围、发布不可变版本、不重置次数 |
| PUT /trade/admin/promotions/{id}/state | trade:promotion:state | PAUSE/RESUME/ARCHIVE |
| GET /trade/admin/promotions/{id} | trade:promotion:query | 当前草稿、activeRule、状态和DB时间阶段 |
| GET /trade/admin/promotions | trade:promotion:list | pageNum/pageSize默认1/20，范围1..1000000/1..100；精确state/code过滤 |

操作者来自已认证且未过期的管理员SecurityUser.userId，忽略请求头中的user-id；不能用会员会话或单独伪造ID替代。RequiresPermissions与显式检查保留，读权限不隐含写/列表权限。请求不接收operatorId/memberId/客户端价格。没有新增租户/部门模型；促销是现有管理员功能权限下的全局数据。

revision/expectedRevision采用0..2147483647 JSON整数；T2初始0沿用，Schema原minimum1修正为0。服务拒绝越界递增，失败事务不留下部分版本。ID、金额分、epoch、次数及total为规范十进制字符串，无前导零/浮点/溢出。activeVersion和priority为JSON整数；nullable字段明确输出null。

四种mutation Req独享PromotionJsonConverter（WebMvcConfigurer注册在普通转换器前）：1MiB body上限，拒绝重复键/未知字段/尾随JSON/标量强制转换；规则严格按类型要求字段，nullable上限也必须出现。其他接口/Jackson不改变。分页拒绝未知或重复参数，查询及分页SQL参数化、有界且稳定排序；不返回完整内部hash/计数/usage或身份信息。

trade_permission脚本：lemnt-server/sql/V20261026__trade_promotion_permissions.sql。六个隐藏F权限条目挂既有trade目录，不新建C页面路由、不覆盖已有配置、不授予角色；按项目方式审核后分配角色。没有虚构React页面已存在。

## 当前管理错误（HTTP与R.code一致）

| 状态 | errorCode | retryable / action |
| --- | --- | --- |
| 400 | INVALID_PROMOTION_REQUEST、AMOUNT_OUT_OF_RANGE | false / FIX_INPUT |
| 401 | ADMIN_LOGIN_REQUIRED | false / LOGIN |
| 403 | PROMOTION_PERMISSION_DENIED | false / FIX_INPUT |
| 404 | PROMOTION_NOT_FOUND | false / REFRESH_ADMIN |
| 409 | PROMOTION_REVISION_CONFLICT、ADMIN_REQUEST_CONFLICT、PROMOTION_STATE_CONFLICT、PROMOTION_LIMIT_BELOW_USAGE | false / REFRESH_ADMIN |
| 409 | PROMOTION_CODE_CONFLICT | false / FIX_INPUT |
| 503 | CATALOG_FACTS_UNAVAILABLE、PRICING_TEMPORARILY_UNAVAILABLE | true / REFRESH_ADMIN |
| 500 | PROMOTION_INTERNAL_ERROR | false / REFRESH_ADMIN |

错误data含errorCode/retryable/action/correlationId，安全msg不暴露内部SQL/异常/凭证。只作用于新管理Controller；旧全局错误及网关可能返回HTTP200+R.code401/503的现状未改。V2报价/恢复的其他错误仍只是设计，不能把REFRESH_ADMIN作为小程序未知提单的处理动作。

## 测试、交接和限制

本地新26项：3个真实商品SQL（精确fen、库存/hash、隐藏分类品牌/下架、范围存在与溢出/无SPU）、3个provider、5个consumer拒绝/故障、14个真实HTTP+交易SQL/事务与AuthLogic权限上下文场景、1个权限脚本重复执行/无页面无角色授予。HTTP用例复用实际T2事务fixture，范围RPC是显式测试桥；提供方与消费方单测、商城SQL测试分别核对契约，不冒充线上Dubbo链路。

PASS：全根Maven verify，71项目、481项测试，0失败/错误/跳过；原455项保留。证据见work-items/yudao-member-mall-trade-migration/evidence/promotion-management。T1和14个历史源码基线保持；T2服务因新增范围验证、分页和修订上限而有预期修改，原T2证据保留为历史，不改写其hash。

目标MySQL/Redis/Nacos/Dubbo、网关/真实管理员Redis会话和角色部署未运行；MockMvc是本地HTTP栈，不是已部署端到端认证证据。React/小程序未改动、未重跑；浏览器/原生编译/设备未验证。没有促销前端、V2报价/缓存提单、价格快照落库或payment/close/outbox/补偿接入。下一步T4继续服务端缓存报价并由缓存生成订单；T5..T7按原依赖推进。保留Draft PR和生产就绪false，回滚保留T2账本/版本/审计。
