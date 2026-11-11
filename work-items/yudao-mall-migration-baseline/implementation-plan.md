# 文件级实现计划预览

> 2026-11-06；状态：用户已确认（原文“确认计划，拆解任务”）。不是已执行任务，也不是授权生成所有门控文件。
> 依据design.md与contract-mapping.md。所有已存在目标文件已检查路径；新增文件是设计候选，不表示已生成。

## 1. 执行门控与依赖

- B0：入口清单已生成，方法语义矩阵仍需补核；112个Controller不等于112项业务已迁移。
- B1a：优先修鉴权/评价地址，当前无DDL/新公开路径。
- B1b：域事实模型与端口 → infra解码及映射测试 → handler引用 → POM移除 → 门禁/全链回归；不同时重写消息机制。
- B2a：类别/筛选查询契约 → 数据读取 → 应用转换 → HTTP与两端 → 回归。createTime排序与虚拟展示销量依赖B2b，未就绪不能假交付。
- B2b：先完成batch-designs/product-data.md，锁定字段、约束、源零售价、旧数据与两端影响，再实施下表门控文件；正式DDL整合最终初始化文件，旧库不自动升级。
- B3：先完成batch-designs/trade-messaging.md：逐条事件及表的去留/业务恢复依据/原子点/文件清单/测试；本预览不授权删Outbox/Inbox或改名绕过约束。
- B4：对其余47目录族补逐方法/页面规则和文件清单；内容/统计等仍在迁移范围，不能自动排到二开。
- B5：按local-acceptance.md确认环境可操作范围后执行；本设计尚未启动或连通任何环境。

## 2. 文件清单

复杂度：★局部适配；★★跨对象/测试；★★★跨表/跨域或规则高影响。每批可独立验证，但共用模型/Mapper/handler的改动必须按依赖串行，不能并发覆盖文件。

### B1a

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/filter/AuthFilter.java` | 修改 | ★★ | 同步真实商品公开读/会员路径，保持身份头清理与令牌域隔离 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterAppRoutesTest.java` | 修改 | ★★ | 测试新路径、错方法、匿名、伪造头 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterDomainTest.java` | 修改 | ★★ | 会员/管理员令牌互斥覆盖新路径 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/ProductAppRouteContractTest.java` | 新增 | ★★ | 新增路由predicate与鉴权filter联合契约，避免只测其一 | 计划确认后可拆任务 |
| `lemnt-miniapp/services/comments.js` | 修改 | ★ | 按GET/POST选择product查询与order投稿地址 | 计划确认后可拆任务 |
| `lemnt-miniapp/tests/comments.test.cjs` | 修改 | ★ | 分别断言读写URL、令牌和响应，不再只覆盖投稿 | 计划确认后可拆任务 |

### B1b

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/model/InboundEventFacts.java` | 新增 | ★★ | 仅本上下文实际消费的typed records；字段映射见契约设计 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/gateway/InboundEventGateway.java` | 新增 | ★ | 声明payload到本域事实的typed解析方法，不引用外部Client | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/main/java/com/lemnt/promotion/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★ | 使用外部协议类型解码并完整映射，保留版本与业务内容 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/test/java/com/lemnt/promotion/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★ | 验证各事件金额/积分/行/业务号映射与非法载荷 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/AfterSaleCompletedEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderClosedEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderCompletedEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderPaidEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/StockPreReservedEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-promotion/lemnt-promotion-application/pom.xml` | 修改 | ★ | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/model/InboundEventFacts.java` | 新增 | ★★ | 仅本上下文实际消费的typed records；字段映射见契约设计 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/gateway/InboundEventGateway.java` | 新增 | ★ | 声明payload到本域事实的typed解析方法，不引用外部Client | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/main/java/com/lemnt/order/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★ | 使用外部协议类型解码并完整映射，保留版本与业务内容 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/test/java/com/lemnt/order/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★ | 验证各事件金额/积分/行/业务号映射与非法载荷 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PayRefundSuccessEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PaySuccessEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-order/lemnt-order-application/pom.xml` | 修改 | ★ | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/model/InboundEventFacts.java` | 新增 | ★★ | 仅本上下文实际消费的typed records；字段映射见契约设计 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/gateway/InboundEventGateway.java` | 新增 | ★ | 声明payload到本域事实的typed解析方法，不引用外部Client | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/main/java/com/lemnt/pay/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★ | 使用外部协议类型解码并完整映射，保留版本与业务内容 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/test/java/com/lemnt/pay/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★ | 验证各事件金额/积分/行/业务号映射与非法载荷 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/event/service/OrderClosedEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-pay/lemnt-pay-application/pom.xml` | 修改 | ★ | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/model/InboundEventFacts.java` | 新增 | ★★ | 仅本上下文实际消费的typed records；字段映射见契约设计 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/gateway/InboundEventGateway.java` | 新增 | ★ | 声明payload到本域事实的typed解析方法，不引用外部Client | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/main/java/com/lemnt/member/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★ | 使用外部协议类型解码并完整映射，保留版本与业务内容 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/test/java/com/lemnt/member/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★ | 验证各事件金额/积分/行/业务号映射与非法载荷 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/event/service/OrderBenefitsEventHandler.java` | 修改 | ★★ | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-member/lemnt-member-application/pom.xml` | 修改 | ★ | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope | 计划确认后可拆任务 |
| `scripts/check_architecture.py` | 修改 | ★★ | 生产/测试scope分辨及外部Client引用规则，现有合法提供者不误报 | 计划确认后可拆任务 |
| `scripts/test_architecture.py` | 修改 | ★★ | 生产/测试scope分辨及外部Client引用规则，现有合法提供者不误报 | 计划确认后可拆任务 |

### B2a

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/StoreFilter.java` | 修改 | ★★ | 补分类集合及排序白名单，兼容现有内部构造 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/gateway/ProductCategoryGateway.java` | 修改 | ★ | 启用分类ids过滤及批量启用直接子分类查询契约 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/gateway/ProductCategoryGatewayImpl.java` | 修改 | ★★ | Lambda过滤+sort/id排序；批量子类不N+1 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/gateway/ProductSpuGatewayImpl.java` | 修改 | ★★ | 转发筛选排序并完整装配已有字段，保留库存/销量写保护 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml` | 修改 | ★★ | 分类IN及固定choose排序，列表/count筛选一致；不动态拼字段 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/qry/StorePageQry.java` | 修改 | ★ | 补可选分类集合及排序参数 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductPageQueryExe.java` | 修改 | ★★ | 输入校验、分类并集与启用直接子分类扩展 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/ProductCategoryEnableListQueryExe.java` | 修改 | ★ | 支持可选ids，沿Gateway顺序输出 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/StoreDetailDTO.java` | 修改 | ★ | 透出已有SPU简介/轮播/配送，不补历史值 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/StoreSkuDTO.java` | 修改 | ★ | 透出已有重量/体积，NULL保持未知 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductDetailQueryExe.java` | 修改 | ★★ | 组装已有SPU/SKU字段，B2a不静默改变详情失败语义 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/req/StorePageReq.java` | 修改 | ★ | 新增optional分类集合及排序；中文Schema/校验 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/converter/StoreReqConverter.java` | 修改 | ★ | HTTP新增参数到本域Qry，保留现有name/页码 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/AppProductCategoryController.java` | 修改 | ★ | 可选ids绑定，查询仍公开且四字段响应保持 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/res/StoreDetailRes.java` | 修改 | ★ | optional展示/配送字段及中文说明 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/res/StoreSkuRes.java` | 修改 | ★ | 重量/体积字段与单位 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/converter/ProductCategoryResConverter.java` | 修改 | ★ | 沿现存转换器补字段，不为名称进行无关拆分 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/test/java/com/lemnt/product/adapter/app/product/StoreHttpContractTest.java` | 修改 | ★★ | 真实绑定、可选参数/非法排序、NULL/字段输出 | 计划确认后可拆任务 |
| `lemnt-server/lemnt-product/lemnt-product-start/src/test/java/com/lemnt/product/start/product/CatalogPersistenceTest.java` | 修改 | ★★ | 分类/价格排序/筛选一致；保留现有库存/历史语义回归 | 计划确认后可拆任务 |
| `lemnt-miniapp/services/storefront.js` | 修改 | ★★ | 扩展查询入参及已有商品字段解码，banner仍归promotion | 计划确认后可拆任务 |
| `lemnt-miniapp/tests/storefront.test.cjs` | 修改 | ★★ | 查询字符串、精确价格、长ID及NULL | 计划确认后可拆任务 |
| `lemnt-miniapp/pages/store/index.js` | 修改 | ★★ | 接入可选排序/分类条件；按已有页面交互增量实现 | 计划确认后可拆任务 |
| `lemnt-miniapp/pages/store/index.wxml` | 修改 | ★★ | 补必要的源选品交互，不换UI框架 | 计划确认后可拆任务 |
| `lemnt-miniapp/pages/product/index.js` | 修改 | ★★ | 使用已透出轮播/简介/配送字段 | 计划确认后可拆任务 |
| `lemnt-miniapp/pages/product/index.wxml` | 修改 | ★★ | 展示源已有资料；不把未知价格/库存补成0 | 计划确认后可拆任务 |
| `lemnt-miniapp/tests/storefrontPages.test.cjs` | 修改 | ★★ | 交互和缺省处理，Node不代表原生平台编译 | 计划确认后可拆任务 |

### B2b

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `lemnt-server/lemnt-product/sql/lemnt_product.sql` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/dataobject/ProductSpuDO.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/dataobject/ProductSkuDO.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/converter/ProductCatalogConverter.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSkuMapper.xml` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductSpu.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductSku.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductCatalogValidation.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/service/ProductCatalogService.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/cmd/ProductSpuCreateCmd.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/cmd/ProductSpuUpdateCmd.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/ProductSpuDTO.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/ProductSkuDTO.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/converter/ProductCatalogDTOConverter.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSpuCreateReq.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSpuUpdateReq.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSkuCreateReq.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/converter/ProductCatalogReqConverter.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/converter/ProductCatalogResConverter.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductDetailQueryExe.java` | 修改 | ★★★ | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL | 前置补充设计/决定完成前禁止生成 |
| `lemnt-ui/src/modules/mall/catalog/components/SpuEditor.tsx` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-ui/src/modules/mall/catalog/components/SpuDetailDrawer.tsx` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-ui/src/modules/mall/catalog/components/SkuSpecFields.tsx` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-ui/src/modules/mall/catalog/api/catalogApi.ts` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-ui/src/modules/mall/catalog/api/catalogApi.test.ts` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-miniapp/services/storefront.js` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-miniapp/pages/product/index.js` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `lemnt-miniapp/pages/product/index.wxml` | 修改 | ★★ | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证 | 前置补充设计/决定完成前禁止生成 |
| `work-items/yudao-mall-migration-baseline/batch-designs/product-data.md` | 新增 | ★★★ | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码 | 前置补充设计/决定完成前禁止生成 |

### B3

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `work-items/yudao-mall-migration-baseline/batch-designs/trade-messaging.md` | 新增 | ★★★ | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码 | 前置补充设计/决定完成前禁止生成 |

### B4

| 文件（相对项目根目录） | 操作 | 复杂度 | 目的 | 执行条件 |
|---|---|---|---|---|
| `work-items/yudao-mall-migration-baseline/batch-designs/promotion-member-statistics.md` | 新增 | ★★★ | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码 | 前置补充设计/决定完成前禁止生成 |

## 3. B3只读输入（不是删除/修改批准）

重点源/目标文件已在source-analysis F06/F07精确引用：源TradeOrderUpdateServiceImpl与库存/活动Handler、PayNotifyServiceImpl；目标OrderRemoteService/OrderBenefitsService/OrderCloseService、PaymentNotificationService、各事件handler、各Outbox/InboxGateway和对应DDL、PayNotifyDispatcher。

在B3补充设计完成之前，不列出伪精确的“可删文件清单”。后续计划必须具体到文件/DDL段/调用点，而不能把本段当作生成指令。

## 4. 当前无变更文件

- lemnt-dependencies/pom.xml：不新增统一商城Client依赖管理；版本缺失在所属infra的当前模式处理，不趁机重做BOM。
- 源yudao三个目录：只读参考，不修改或运行其脚本。
- system业务源码/初始化SQL：无与本批运行阻塞直接相关的证据时不改。
- docs/config/commerce-gateway-routes.yaml、既有端点JSON：B1a只修鉴权与调用地址，原正确GET/POST归属不重写；若B2真实新增路径，再由具体批次列入变更。

## 5. 验证与回退

- B1a：新路径鉴权+路由联合测试；小程序GET/POST不同URL、令牌、防伪造与旧页面回归。
- B1b：每类事件字段映射、缺字段/未知协议失败、原业务回归；生产依赖和源码引用双门禁。
- B2a：真实Mapper筛选/count一致、分类稳定顺序、已有资料/NULL和HTTP输出、两端交互。
- B2b/B3：必须新增源对应业务用例、失败恢复及真实MySQL/MQ验证，不能只改测试基线。
- 版本回退只回本批受控变更，保留用户新增改动；不执行git reset/clean、删旧库或恢复索引。跨服务契约变更采取兼容读写和明确发布顺序。

## 6. 预览确认的含义

确认后可进入cs-task，将就绪批次和受阻批次的补充设计分别拆解；**不表示设计阶段已经完成全部源业务方法审计，也不允许cs-generate直接猜测门控文件**。全量迁移验收仍要覆盖capability-matrix全部范围。
