# 迁移基线与运行验收——正式任务清单

> 日期：2026-11-06；状态：用户已确认任务范围、依赖与并行策略（2026-11-06T09:43:31+08:00）；B1a、B1b已完成自动化验证，整体迁移未完成。
> 任务确认原文：“确认任务草案”。确认不放开未来人工门控，不等同于环境操作授权；当前执行记录见第8、9节。
> 未使用模板文件：项目templates/task_template.md不存在，采用skill内置格式。

## 1. 数量和状态口径

- 已确认计划的95条文件操作逐条映射为F001–F095，未漏项；另加11项准备/验证/环境证据任务和6个人工门控。
- 表内“初态”保留拆解时记录；当前执行状态以第8、9、10节及evidence/tasks.json为准，pending不代表依赖已经满足，blocked仍须审批和实际前置任务。
- 32条原门控操作中，3项是可先准备的补充设计文档；其余29项商品业务文件仍受G-B2b门控。B3/B4业务实现节点尚待补充设计追加，不冒称已完成逐文件拆解。
- 预估为文件复杂度对应的人工作业量预算，非模型运行时间、交付承诺或并行后的日历时长；受阻工作重新估算，不汇总成虚假固定工期。

## 2. 首批B1a（默认先执行）

- 鉴权支线：P000 → F001 → F002 / F003 → F004。
- 小程序支线：P000 → F005 → F006。
- 两条支线文件不重叠，可分工；V-B1a实际回归通过后才扩展B1b/B2a。无需DDL、生产配置发布或重做消息框架。

## 3. 文件任务表（按拓扑顺序执行，编号不代表硬性线性顺序）

### 准备

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| P000 | `work-items/yudao-mall-migration-baseline/evidence/execution-baseline.json` | 新增 | ★☆☆ | 无 | 10–20人工作业分钟 | pending | 记录当前HEAD、工作树、源引用指纹、文件存在性与环境授权边界，不执行构建/部署；任务草案已确认，仍须执行指令与前置完成 |

### B1a

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F001 | `lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/filter/AuthFilter.java` | 修改 | ★★☆ | P000 | 30–60人工作业分钟 | pending | 同步真实商品公开读/会员路径，保持身份头清理与令牌域隔离；任务草案已确认，仍须执行指令与前置完成 |
| F002 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterAppRoutesTest.java` | 修改 | ★★☆ | F001 | 30–60人工作业分钟 | pending | 测试新路径、错方法、匿名、伪造头；任务草案已确认，仍须执行指令与前置完成 |
| F003 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterDomainTest.java` | 修改 | ★★☆ | F001 | 30–60人工作业分钟 | pending | 会员/管理员令牌互斥覆盖新路径；任务草案已确认，仍须执行指令与前置完成 |
| F004 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/ProductAppRouteContractTest.java` | 新增 | ★★☆ | F001, F002, F003 | 30–60人工作业分钟 | pending | 新增路由predicate与鉴权filter联合契约，避免只测其一；任务草案已确认，仍须执行指令与前置完成 |
| F005 | `lemnt-miniapp/services/comments.js` | 修改 | ★☆☆ | P000 | 10–20人工作业分钟 | pending | 按GET/POST选择product查询与order投稿地址；任务草案已确认，仍须执行指令与前置完成 |
| F006 | `lemnt-miniapp/tests/comments.test.cjs` | 修改 | ★☆☆ | F005 | 10–20人工作业分钟 | pending | 分别断言读写URL、令牌和响应，不再只覆盖投稿；任务草案已确认，仍须执行指令与前置完成 |

### B1b

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F007 | `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/model/InboundEventFacts.java` | 新增 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 仅本上下文实际消费的typed records；字段映射见契约设计；任务草案已确认，仍须执行指令与前置完成 |
| F008 | `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/gateway/InboundEventGateway.java` | 新增 | ★☆☆ | F007 | 10–20人工作业分钟 | pending | 声明payload到本域事实的typed解析方法，不引用外部Client；任务草案已确认，仍须执行指令与前置完成 |
| F009 | `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/main/java/com/lemnt/promotion/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★☆ | F007, F008 | 30–60人工作业分钟 | pending | 使用外部协议类型解码并完整映射，保留版本与业务内容；任务草案已确认，仍须执行指令与前置完成 |
| F010 | `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/test/java/com/lemnt/promotion/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★☆ | F009 | 30–60人工作业分钟 | pending | 验证各事件金额/积分/行/业务号映射与非法载荷；任务草案已确认，仍须执行指令与前置完成 |
| F011 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/AfterSaleCompletedEventHandler.java` | 修改 | ★★☆ | F008 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F012 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderClosedEventHandler.java` | 修改 | ★★☆ | F008 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F013 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderCompletedEventHandler.java` | 修改 | ★★☆ | F008 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F014 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderPaidEventHandler.java` | 修改 | ★★☆ | F008 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F015 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/StockPreReservedEventHandler.java` | 修改 | ★★☆ | F008 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F016 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/pom.xml` | 修改 | ★☆☆ | F010, F011, F012, F013, F014, F015 | 10–20人工作业分钟 | pending | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope；任务草案已确认，仍须执行指令与前置完成 |
| F017 | `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/model/InboundEventFacts.java` | 新增 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 仅本上下文实际消费的typed records；字段映射见契约设计；任务草案已确认，仍须执行指令与前置完成 |
| F018 | `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/gateway/InboundEventGateway.java` | 新增 | ★☆☆ | F017 | 10–20人工作业分钟 | pending | 声明payload到本域事实的typed解析方法，不引用外部Client；任务草案已确认，仍须执行指令与前置完成 |
| F019 | `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/main/java/com/lemnt/order/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★☆ | F017, F018 | 30–60人工作业分钟 | pending | 使用外部协议类型解码并完整映射，保留版本与业务内容；任务草案已确认，仍须执行指令与前置完成 |
| F020 | `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/test/java/com/lemnt/order/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★☆ | F019 | 30–60人工作业分钟 | pending | 验证各事件金额/积分/行/业务号映射与非法载荷；任务草案已确认，仍须执行指令与前置完成 |
| F021 | `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PayRefundSuccessEventHandler.java` | 修改 | ★★☆ | F018 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F022 | `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PaySuccessEventHandler.java` | 修改 | ★★☆ | F018 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F023 | `lemnt-server/lemnt-order/lemnt-order-application/pom.xml` | 修改 | ★☆☆ | F020, F021, F022 | 10–20人工作业分钟 | pending | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope；任务草案已确认，仍须执行指令与前置完成 |
| F024 | `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/model/InboundEventFacts.java` | 新增 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 仅本上下文实际消费的typed records；字段映射见契约设计；任务草案已确认，仍须执行指令与前置完成 |
| F025 | `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/gateway/InboundEventGateway.java` | 新增 | ★☆☆ | F024 | 10–20人工作业分钟 | pending | 声明payload到本域事实的typed解析方法，不引用外部Client；任务草案已确认，仍须执行指令与前置完成 |
| F026 | `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/main/java/com/lemnt/pay/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★☆ | F024, F025 | 30–60人工作业分钟 | pending | 使用外部协议类型解码并完整映射，保留版本与业务内容；任务草案已确认，仍须执行指令与前置完成 |
| F027 | `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/test/java/com/lemnt/pay/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★☆ | F026 | 30–60人工作业分钟 | pending | 验证各事件金额/积分/行/业务号映射与非法载荷；任务草案已确认，仍须执行指令与前置完成 |
| F028 | `lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/event/service/OrderClosedEventHandler.java` | 修改 | ★★☆ | F025 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F029 | `lemnt-server/lemnt-pay/lemnt-pay-application/pom.xml` | 修改 | ★☆☆ | F027, F028 | 10–20人工作业分钟 | pending | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope；任务草案已确认，仍须执行指令与前置完成 |
| F030 | `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/model/InboundEventFacts.java` | 新增 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 仅本上下文实际消费的typed records；字段映射见契约设计；任务草案已确认，仍须执行指令与前置完成 |
| F031 | `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/gateway/InboundEventGateway.java` | 新增 | ★☆☆ | F030 | 10–20人工作业分钟 | pending | 声明payload到本域事实的typed解析方法，不引用外部Client；任务草案已确认，仍须执行指令与前置完成 |
| F032 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/main/java/com/lemnt/member/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 | ★★☆ | F030, F031 | 30–60人工作业分钟 | pending | 使用外部协议类型解码并完整映射，保留版本与业务内容；任务草案已确认，仍须执行指令与前置完成 |
| F033 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/test/java/com/lemnt/member/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 | ★★☆ | F032 | 30–60人工作业分钟 | pending | 验证各事件金额/积分/行/业务号映射与非法载荷；任务草案已确认，仍须执行指令与前置完成 |
| F034 | `lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/event/service/OrderBenefitsEventHandler.java` | 修改 | ★★☆ | F031 | 30–60人工作业分钟 | pending | 换成本域解析端口/模型；原事务、幂等与业务判定不顺带重构；任务草案已确认，仍须执行指令与前置完成 |
| F035 | `lemnt-server/lemnt-member/lemnt-member-application/pom.xml` | 修改 | ★☆☆ | F033, F034 | 10–20人工作业分钟 | pending | 类型引用清零后删除本模块外部Client生产声明，保留本服务Client和test scope；任务草案已确认，仍须执行指令与前置完成 |
| F036 | `scripts/check_architecture.py` | 修改 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 生产/测试scope分辨及外部Client引用规则，现有合法提供者不误报；任务草案已确认，仍须执行指令与前置完成 |
| F037 | `scripts/test_architecture.py` | 修改 | ★★☆ | F036 | 30–60人工作业分钟 | pending | 生产/测试scope分辨及外部Client引用规则，现有合法提供者不误报；任务草案已确认，仍须执行指令与前置完成 |

### B2a

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F038 | `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/StoreFilter.java` | 修改 | ★★☆ | V-B1a | 30–60人工作业分钟 | pending | 补分类集合及排序白名单，兼容现有内部构造；任务草案已确认，仍须执行指令与前置完成 |
| F039 | `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/gateway/ProductCategoryGateway.java` | 修改 | ★☆☆ | V-B1a | 10–20人工作业分钟 | pending | 启用分类ids过滤及批量启用直接子分类查询契约；任务草案已确认，仍须执行指令与前置完成 |
| F040 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/gateway/ProductCategoryGatewayImpl.java` | 修改 | ★★☆ | F039 | 30–60人工作业分钟 | pending | Lambda过滤+sort/id排序；批量子类不N+1；任务草案已确认，仍须执行指令与前置完成 |
| F041 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/gateway/ProductSpuGatewayImpl.java` | 修改 | ★★☆ | F038 | 30–60人工作业分钟 | pending | 转发筛选排序并完整装配已有字段，保留库存/销量写保护；任务草案已确认，仍须执行指令与前置完成 |
| F042 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml` | 修改 | ★★☆ | F038 | 30–60人工作业分钟 | pending | 分类IN及固定choose排序，列表/count筛选一致；不动态拼字段；任务草案已确认，仍须执行指令与前置完成 |
| F043 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/qry/StorePageQry.java` | 修改 | ★☆☆ | F038 | 10–20人工作业分钟 | pending | 补可选分类集合及排序参数；任务草案已确认，仍须执行指令与前置完成 |
| F044 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductPageQueryExe.java` | 修改 | ★★☆ | F043, F038, F039, F040, F042, F041 | 30–60人工作业分钟 | pending | 输入校验、分类并集与启用直接子分类扩展；任务草案已确认，仍须执行指令与前置完成 |
| F045 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/ProductCategoryEnableListQueryExe.java` | 修改 | ★☆☆ | F039, F040 | 10–20人工作业分钟 | pending | 支持可选ids，沿Gateway顺序输出；任务草案已确认，仍须执行指令与前置完成 |
| F046 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/StoreDetailDTO.java` | 修改 | ★☆☆ | V-B1a | 10–20人工作业分钟 | pending | 透出已有SPU简介/轮播/配送，不补历史值；任务草案已确认，仍须执行指令与前置完成 |
| F047 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/StoreSkuDTO.java` | 修改 | ★☆☆ | V-B1a | 10–20人工作业分钟 | pending | 透出已有重量/体积，NULL保持未知；任务草案已确认，仍须执行指令与前置完成 |
| F048 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductDetailQueryExe.java` | 修改 | ★★☆ | F046, F047 | 30–60人工作业分钟 | pending | 组装已有SPU/SKU字段，B2a不静默改变详情失败语义；任务草案已确认，仍须执行指令与前置完成 |
| F049 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/req/StorePageReq.java` | 修改 | ★☆☆ | V-B1a | 10–20人工作业分钟 | pending | 新增optional分类集合及排序；中文Schema/校验；任务草案已确认，仍须执行指令与前置完成 |
| F050 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/converter/StoreReqConverter.java` | 修改 | ★☆☆ | F049, F043 | 10–20人工作业分钟 | pending | HTTP新增参数到本域Qry，保留现有name/页码；任务草案已确认，仍须执行指令与前置完成 |
| F051 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/AppProductCategoryController.java` | 修改 | ★☆☆ | F045 | 10–20人工作业分钟 | pending | 可选ids绑定，查询仍公开且四字段响应保持；任务草案已确认，仍须执行指令与前置完成 |
| F052 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/res/StoreDetailRes.java` | 修改 | ★☆☆ | F046 | 10–20人工作业分钟 | pending | optional展示/配送字段及中文说明；任务草案已确认，仍须执行指令与前置完成 |
| F053 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/res/StoreSkuRes.java` | 修改 | ★☆☆ | F047 | 10–20人工作业分钟 | pending | 重量/体积字段与单位；任务草案已确认，仍须执行指令与前置完成 |
| F054 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/converter/ProductCategoryResConverter.java` | 修改 | ★☆☆ | F052, F053, F046, F047 | 10–20人工作业分钟 | pending | 沿现存转换器补字段，不为名称进行无关拆分；任务草案已确认，仍须执行指令与前置完成 |
| F055 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/test/java/com/lemnt/product/adapter/app/product/StoreHttpContractTest.java` | 修改 | ★★☆ | F038, F039, F040, F041, F042, F043, F044, F045, F046, F047, F048, F049, F050, F051, F052, F053, F054 | 30–60人工作业分钟 | pending | 真实绑定、可选参数/非法排序、NULL/字段输出；任务草案已确认，仍须执行指令与前置完成 |
| F056 | `lemnt-server/lemnt-product/lemnt-product-start/src/test/java/com/lemnt/product/start/product/CatalogPersistenceTest.java` | 修改 | ★★☆ | F040, F042, F041, F044, F048 | 30–60人工作业分钟 | pending | 分类/价格排序/筛选一致；保留现有库存/历史语义回归；任务草案已确认，仍须执行指令与前置完成 |
| F057 | `lemnt-miniapp/services/storefront.js` | 修改 | ★★☆ | F050, F054 | 30–60人工作业分钟 | pending | 扩展查询入参及已有商品字段解码，banner仍归promotion；任务草案已确认，仍须执行指令与前置完成 |
| F058 | `lemnt-miniapp/tests/storefront.test.cjs` | 修改 | ★★☆ | F057 | 30–60人工作业分钟 | pending | 查询字符串、精确价格、长ID及NULL；任务草案已确认，仍须执行指令与前置完成 |
| F059 | `lemnt-miniapp/pages/store/index.js` | 修改 | ★★☆ | F057 | 30–60人工作业分钟 | pending | 接入可选排序/分类条件；按已有页面交互增量实现；任务草案已确认，仍须执行指令与前置完成 |
| F060 | `lemnt-miniapp/pages/store/index.wxml` | 修改 | ★★☆ | F059 | 30–60人工作业分钟 | pending | 补必要的源选品交互，不换UI框架；任务草案已确认，仍须执行指令与前置完成 |
| F061 | `lemnt-miniapp/pages/product/index.js` | 修改 | ★★☆ | F057 | 30–60人工作业分钟 | pending | 使用已透出轮播/简介/配送字段；任务草案已确认，仍须执行指令与前置完成 |
| F062 | `lemnt-miniapp/pages/product/index.wxml` | 修改 | ★★☆ | F061 | 30–60人工作业分钟 | pending | 展示源已有资料；不把未知价格/库存补成0；任务草案已确认，仍须执行指令与前置完成 |
| F063 | `lemnt-miniapp/tests/storefrontPages.test.cjs` | 修改 | ★★☆ | F060, F062, F058 | 30–60人工作业分钟 | pending | 交互和缺省处理，Node不代表原生平台编译；任务草案已确认，仍须执行指令与前置完成 |

### B2b

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F064 | `lemnt-server/lemnt-product/sql/lemnt_product.sql` | 修改 | ★★★ | V-B2a, G-B2b | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F065 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/dataobject/ProductSpuDO.java` | 修改 | ★★★ | V-B2a, G-B2b, F064 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F066 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/dataobject/ProductSkuDO.java` | 修改 | ★★★ | V-B2a, G-B2b, F065 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F067 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/converter/ProductCatalogConverter.java` | 修改 | ★★★ | V-B2a, G-B2b, F066 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F068 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml` | 修改 | ★★★ | V-B2a, G-B2b, F067, F042 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F069 | `lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSkuMapper.xml` | 修改 | ★★★ | V-B2a, G-B2b, F068 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F070 | `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductSpu.java` | 修改 | ★★★ | V-B2a, G-B2b, F069 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F071 | `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductSku.java` | 修改 | ★★★ | V-B2a, G-B2b, F070 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F072 | `lemnt-server/lemnt-product/lemnt-product-domain/src/main/java/com/lemnt/product/domain/product/model/ProductCatalogValidation.java` | 修改 | ★★★ | V-B2a, G-B2b, F071 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F073 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/service/ProductCatalogService.java` | 修改 | ★★★ | V-B2a, G-B2b, F072 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F074 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/cmd/ProductSpuCreateCmd.java` | 修改 | ★★★ | V-B2a, G-B2b, F073 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F075 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/cmd/ProductSpuUpdateCmd.java` | 修改 | ★★★ | V-B2a, G-B2b, F074 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F076 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/ProductSpuDTO.java` | 修改 | ★★★ | V-B2a, G-B2b, F075 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F077 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/dto/ProductSkuDTO.java` | 修改 | ★★★ | V-B2a, G-B2b, F076 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F078 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/converter/ProductCatalogDTOConverter.java` | 修改 | ★★★ | V-B2a, G-B2b, F077 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F079 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSpuCreateReq.java` | 修改 | ★★★ | V-B2a, G-B2b, F078 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F080 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSpuUpdateReq.java` | 修改 | ★★★ | V-B2a, G-B2b, F079 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F081 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/req/ProductSkuCreateReq.java` | 修改 | ★★★ | V-B2a, G-B2b, F080 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F082 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/converter/ProductCatalogReqConverter.java` | 修改 | ★★★ | V-B2a, G-B2b, F081 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F083 | `lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/web/product/converter/ProductCatalogResConverter.java` | 修改 | ★★★ | V-B2a, G-B2b, F082 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F084 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/executor/StoreProductDetailQueryExe.java` | 修改 | ★★★ | V-B2a, G-B2b, F083, F048 | 60–120人工作业分钟（完成前置后重估） | blocked | 按批准字段/输入/购买/浏览方案贯通；不得仅加列或改变历史NULL；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F085 | `lemnt-ui/src/modules/mall/catalog/components/SpuEditor.tsx` | 修改 | ★★☆ | V-B2a, G-B2b, F084 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F086 | `lemnt-ui/src/modules/mall/catalog/components/SpuDetailDrawer.tsx` | 修改 | ★★☆ | V-B2a, G-B2b, F085 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F087 | `lemnt-ui/src/modules/mall/catalog/components/SkuSpecFields.tsx` | 修改 | ★★☆ | V-B2a, G-B2b, F086 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F088 | `lemnt-ui/src/modules/mall/catalog/api/catalogApi.ts` | 修改 | ★★☆ | V-B2a, G-B2b, F087 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F089 | `lemnt-ui/src/modules/mall/catalog/api/catalogApi.test.ts` | 修改 | ★★☆ | V-B2a, G-B2b, F088 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F090 | `lemnt-miniapp/services/storefront.js` | 修改 | ★★☆ | V-B2a, G-B2b, F089, F057 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F091 | `lemnt-miniapp/pages/product/index.js` | 修改 | ★★☆ | V-B2a, G-B2b, F090, F061 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F092 | `lemnt-miniapp/pages/product/index.wxml` | 修改 | ★★☆ | V-B2a, G-B2b, F091, F062 | 30–60人工作业分钟 | blocked | 源配置/展示字段及精确金额/编号贯通，与后台/商品规则同批验证；G-B2b与V-B2a完成，补充设计含完整测试任务且无未定字段 |
| F093 | `work-items/yudao-mall-migration-baseline/batch-designs/product-data.md` | 新增 | ★★★ | P000 | 60–120人工作业分钟（完成前置后重估） | pending | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码；可准备补充设计，但不能据此直接生成门控业务代码 |

### B3

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F094 | `work-items/yudao-mall-migration-baseline/batch-designs/trade-messaging.md` | 新增 | ★★★ | P000 | 60–120人工作业分钟（完成前置后重估） | pending | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码；可准备补充设计，但不能据此直接生成门控业务代码 |

### B4

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| F095 | `work-items/yudao-mall-migration-baseline/batch-designs/promotion-member-statistics.md` | 新增 | ★★★ | P000 | 60–120人工作业分钟（完成前置后重估） | pending | 补逐方法/字段/恢复依据和文件清单，审核通过后才能生成相应业务代码；可准备补充设计，但不能据此直接生成门控业务代码 |

### 验证

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| V-B1a | `work-items/yudao-mall-migration-baseline/evidence/b1a-validation.json` | 新增 | ★★☆ | F001, F002, F003, F004, F005, F006 | 30–60人工作业分钟 | pending | 执行新路径公开/会员/错身份/防伪造与GET/POST不同URL回归，记录实际命令/退出码，不只写文件；任务草案已确认，仍须执行指令与前置完成 |
| V-B1b | `work-items/yudao-mall-migration-baseline/evidence/b1b-validation.json` | 新增 | ★★★ | F007, F008, F009, F010, F011, F012, F013, F014, F015, F016, F017, F018, F019, F020, F021, F022, F023, F024, F025, F026, F027, F028, F029, F030, F031, F032, F033, F034, F035, F036, F037 | 60–120人工作业分钟 | pending | 四域事件完整映射/原事务幂等回归、生产依赖与源码引用门禁、检查器自测、全Reactor编译测试；只改协议边界；任务草案已确认，仍须执行指令与前置完成 |
| V-B2a | `work-items/yudao-mall-migration-baseline/evidence/b2a-validation.json` | 新增 | ★★★ | F038, F039, F040, F041, F042, F043, F044, F045, F046, F047, F048, F049, F050, F051, F052, F053, F054, F055, F056, F057, F058, F059, F060, F061, F062, F063 | 60–120人工作业分钟 | pending | 真实Mapper多分类/筛选count一致/price及现有salesCount排序，分类ids与稳定排序，已有字段NULL透出、两端交互与格式回归；不假交付createTime/虚拟销量；任务草案已确认，仍须执行指令与前置完成 |
| V-B2b | `work-items/yudao-mall-migration-baseline/evidence/b2b-validation.json` | 新增 | ★★★ | F064, F065, F066, F067, F068, F069, F070, F071, F072, F073, F074, F075, F076, F077, F078, F079, F080, F081, F082, F083, F084, F085, F086, F087, F088, F089, F090, F091, F092 | 前置补充设计完成后重估 | blocked | 按补充设计追加的测试任务完成后验证商品新字段/输入/可售/零金额链与两端；不得仅执行旧测试；G-B2b必须包含新增测试节点；该图更新前不能执行 |

### 环境准备

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| E001 | `work-items/yudao-mall-migration-baseline/local-environment-preflight.md` | 新增 | ★★☆ | P000 | 30–60人工作业分钟 | pending | 只读核对中间件/端口/版本、独立库和namespace策略、渠道条件；不启动/改已有环境；任务草案已确认，仍须执行指令与前置完成 |
| E002 | `work-items/yudao-mall-migration-baseline/local-acceptance.md` | 修改 | ★★☆ | E001 | 30–60人工作业分钟 | pending | 将探测结果填入明确的隔离配置/启动及回退步骤，不保存凭据，不把文档存在当环境就绪；任务草案已确认，仍须执行指令与前置完成 |

### 本地验收

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| E003 | `work-items/yudao-mall-migration-baseline/evidence/local-infrastructure-validation.json` | 新增 | ★★★ | G-ENV | 环境条件确认后重估 | blocked | 在授权隔离环境执行中间件/空库配置与连通性验证，记录实际命令和结果；不接生产；G-ENV通过；无DB/中间件操作默认授权 |
| E004 | `work-items/yudao-mall-migration-baseline/evidence/local-startup-validation.json` | 新增 | ★★★ | E003, V-B1a | 环境条件确认后重估 | blocked | 启动相关provider/网关/两端并验证实际注册、RPC/HTTP/身份；仅验证启动底座，不宣称交易迁移完成；G-ENV与隔离基础设施验证完成 |
| E005 | `work-items/yudao-mall-migration-baseline/evidence/local-core-flow-validation.json` | 新增 | ★★★ | G-CORE | 渠道与补充任务确认后重估 | blocked | 实际执行购买/关单/履约/评价/售后等主线与异常，记录DB/消息/页面结果，替身与真实渠道分开；G-CORE实际前置任务完成；不是设计确认即授权真实资金操作 |

### 迁移收口

| 编号 | 文件（相对项目根） | 操作 | 复杂度 | 依赖 | 作业量预算 | 初态 | 任务/完成条件 |
|---|---|---|---|---|---|---|---|
| E006 | `work-items/yudao-mall-migration-baseline/evidence/full-migration-acceptance.json` | 新增 | ★★★ | G-ALL | 全量补充清单形成后重估 | blocked | 汇总全量能力及分层证据；任何范围内未核实/未实现/未验收仍未完成，二开另列；G-ALL对应的实际实现与验收全部完成 |

## 4. 人工门控（不是一次性代码授权）

| 门控 | 依赖 | 通过条件 | 记录文件 |
|---|---|---|---|
| G-B2b | F093 | 字段/约束/历史NULL/售价零元/浏览副作用/所有新增与修改测试文件及任务已写全，用户批准 | `work-items/yudao-mall-migration-baseline/decision-log.md` |
| G-B3 | F094 | 逐事件成功/失败/未决事实、回查/业务幂等、表去留、完整代码与测试文件清单获用户批准 | `work-items/yudao-mall-migration-baseline/decision-log.md` |
| G-B4 | F095 | 47族逐方法/页面差异和完整剩余实现/测试任务已补齐并获用户批准 | `work-items/yudao-mall-migration-baseline/decision-log.md` |
| G-ENV | E002 | 用户确认本机可丢弃/隔离实例、独立库/端口/数据目录及允许操作范围；已有业务库/积压不得操作 | `work-items/yudao-mall-migration-baseline/decision-log.md` |
| G-CORE | V-B1b, V-B2a, V-B2b, G-B3, E004 | B3完整实现/测试节点已追加且实际完成，核心矩阵达到可验收；支付等外部条件按实际标记；不得只凭本门控被批准跳过实现 | `work-items/yudao-mall-migration-baseline/decision-log.md` |
| G-ALL | G-B4, E005 | 剩余全部源能力实现/回归/本地验收节点已追加并完成，所有未知/阻塞/排除有真实记录；不能以112入口数替代业务覆盖 | `work-items/yudao-mall-migration-baseline/decision-log.md` |

## 5. 依赖关系与并行策略

| 任务组 | 可并行条件 | 必须串行/等待 |
|---|---|---|
| B1a鉴权 / 评价客户端 | P000通过后，两个文件集合独立 | V-B1a汇总两条支线实际测试 |
| B1b的promotion/order/pay/member协议隔离 | V-B1a通过；每组只写自己的域/infra/handler/POM | 每组模型→端口→infra；映射/handler验证后再移POM依赖；全组完成再V-B1b |
| B2a分类/已有详情输出与B1b | V-B1a通过后，可在不重叠文件上分工 | 公共Filter/Mapper/Converter合同先锁定；不得同时编辑同文件；V-B2a做组合回归 |
| 补充设计F093/F094/F095与首批代码 | 只准备设计资料，引用当前稳定基线 | 输出decision-log及全量矩阵等共享资料时单写者；不能把文档写完当门控已批准 |
| B2b商品业务 | 当前按计划顺序保守串行 | 等V-B2a与G-B2b；后续详细图获批前不重新并行拆文件 |
| 环境只读预检 | P000后可独立核对 | E003及后续任何基础设施操作均需G-ENV确认 |
| 真实核心与全量验收 | 环境、实际实现、补充测试节点均完成 | G-CORE/G-ALL不是空白许可；缺外部条件明确blocked，不自动放行 |

已对任务与人工门控组成的有向依赖图进行拓扑校验：无环、无缺失依赖；同一路径跨批次的任务均补上前序写入依赖。依赖是任务实施顺序，不改变Maven层级关系。

## 6. 执行时必须保留的边界

- Source三个目录只读；当前用户改动/索引不覆盖，不commit/push。
- B1b只做外部协议隔离，保留原事务、事件号、幂等/乱序和业务校验；不为该任务新增消息表或同步重写消息链。
- B2a先交price和已有真实salesCount排序、启用分类与已有字段输出。createTime及虚拟销量数据未就绪前，不向客户端宣传可用的完整排序集；后续B2b同步扩展Schema/请求/实现/测试，不新增临时开关掩盖未实现。
- 新增商品配置/价格约束、交易内部时序和消息/业务记录去留，需要对应门控；不能仅为测试绿而恢复用户否定的product会员RPC或放宽断言。
- 自动化证据、真实MySQL/MQ/Redis、浏览器/微信工具/沙箱证据各记各的；B1a仅做自动化契约回归，不声称真实环境已验证。
- B2b/B3/B4的全量测试与新增接口/文件清单未补齐前，cs-generate不得自行猜测；若后续变更计划，先更新task-draft/正式tasks及其审批记录。

## 7. 确认记录与下一阶段

- 用户确认原文：“确认任务草案”。
- 确认时间：2026-11-06T09:43:31+08:00。
- 已确认任务、依赖关系和并行策略保持不变，原tasks-draft.md与task-draft.json保留作确认前记录。
- task阶段标记completed，正式产物为本文件及evidence/tasks.json。B1a执行记录见第8节；其余实施任务保持pending或blocked，人工门控仍待后续明确批准。
- 首轮仅执行P000、B1a的F001–F006及V-B1a验证；B1b后续确认与完成记录见第9节，未执行B2a及后续批次、DDL或环境变更。

## 8. B1a执行记录（2026-11-09）

用户已确认生成预览：“确认预览并执行”。执行基线HEAD为`12745a285d36869f2005d9f4ffdada93351ec1e4`，工作树初始干净，45处源码引用指纹均未变。

| 任务 | 当前状态 | 实际结果 |
|---|---|---|
| P000 | completed | 记录执行基线、引用指纹与授权边界 |
| F001 | completed | `/app`命名空间选择会员域；仅精确商品GET公开，删除失效旧商品公开地址 |
| F002 | completed | 新地址、错方法、近似路径、非法ID与伪造头回归 |
| F003 | completed | 真实JWT验证新路径的身份域互斥，后台宽泛白名单不能绕过会员鉴权 |
| F004 | completed | 生产YAML路由谓词与AuthFilter联合契约，覆盖四类公开读及十类会员读写 |
| F005 | completed | GET `/app/comments`、POST `/mall/app/comments`分离 |
| F006 | completed | 分别断言读写URL/方法/令牌，未登录可读而不可投稿 |
| V-B1a | completed | 网关365项、小程序182项全部通过；架构与JS格式、差异空白检查通过 |

- 证据：[执行基线](evidence/execution-baseline.json)、[B1a自动化验证](evidence/b1a-validation.json)、[代码摘要](code-summary.md)。修复前先复现失败，再对最终代码执行完整回归。
- 固定Java格式工具已核验SHA256，但运行被权限策略拒绝；手工保持局部风格，不声明Java自动格式检查通过。
- B1a结束时106个文件任务：8 completed、64 pending、34 blocked；6个人工门控状态不变。generate总体仍为in_progress，不将B1a结果扩大为整个迁移已完成。
- 未进行真实基础设施/微信工具/真机验收，未执行DDL、commit、push或修改索引。

## 9. B1b执行范围补充（2026-11-09）

用户已确认35文件生成预览：“确认并执行”。范围为F007–F037及以下4个必要配套文件，V-B1b在全部前置实际完成后验证；B2a及后续批次不在本轮执行范围。

| 任务 | 文件 | 操作 | 依赖 | 完成条件 |
|---|---|---|---|---|
| B1b-T01 | `lemnt-server/lemnt-order/lemnt-order-application/src/test/java/com/lemnt/order/application/event/PaySuccessEventHandlerTest.java` | 修改 | F019、F022 | 适配解析端口注入，保留原支付业务断言 |
| B1b-T02 | `lemnt-server/lemnt-pay/lemnt-pay-start/src/test/java/com/lemnt/pay/start/event/CashPaymentPersistenceTest.java` | 修改 | F026、F028 | 适配关单构造器，保留回滚、渠道失败及幂等断言 |
| B1b-T03 | `lemnt-server/lemnt-member/lemnt-member-start/src/test/java/com/lemnt/member/start/MemberAccountIntegrationTest.java` | 修改 | F032、F034 | 适配会员事件构造器，保留原权益事务回归 |
| B1b-T04 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/pom.xml` | 修改 | V-B1a | 仅补映射测试所需test依赖，不新增运行时依赖 |

F033依赖追加B1b-T04，V-B1b依赖追加全部4项；共110个文件任务，拓扑图已校验无环、无缺失。B1a成果按指纹保留，写入前状态及文件哈希见`evidence/b1b-execution-baseline.json`。本批仍不改变JSON协议、Topic、事务、inbox幂等键、冻结业务规则或消息表，不执行DDL、提交或修改索引。

### B1b完成记录

- F007–F037、B1b-T01–T04及V-B1b共36项已完成，35个代码/测试/POM/脚本文件全部落地。4个application共6处外部Client生产声明移除，原本服务Client和test scope依赖保留。
- 四域映射测试26项通过；全服务Reactor执行`clean test`，1338项零失败、错误、跳过；脚本33项、小程序182项通过。新增版本/类型拒绝测试在解析及消费登记之前生效，原回滚、重复和乱序业务测试保留。
- 生产源码与POM双门禁零违规；逐事件字段名/顺序与真实Client协议一致，事务注解、消费键与原payload路径保持。B1a指纹、HEAD与索引未变。
- 证据：[B1b验证](evidence/b1b-validation.json)、[代码摘要](code-summary.md)。固定Java格式工具此前被权限策略拦截，本批未绕过或重试，自动AOSP格式验证仍未完成。
- 当前110个文件任务：44 completed、32 pending、34 blocked；6个人工门控保持原状态。generate总体仍为in_progress，B2a及后续批次和真实环境/微信/渠道验收未执行。

## 10. 基线对齐与运行验收预检（2026-11-09）

- 本轮按“对齐迁移基线与运行验收”执行现有成果复核和只读环境预检，不扩展B2a业务实现或未来人工门控。
- B1a/B1b验证证据共42条文件指纹与当前工作区一致；HEAD仍为`12745a285d36869f2005d9f4ffdada93351ec1e4`。上游三个参考目录的全量源码语义和指纹不在本轮重新核验范围，不能据此宣称全部源能力已迁移。
- 修正design/decision-log中“待计划确认”“未实施”“当前进入cs-task”等当前态歧义，保留2026-11-06设计快照与历史测试事实。
- E001 completed：新增[只读环境预检](local-environment-preflight.md)。默认Java11，已安装Java17；初检Docker不可达，复核已见MySQL/Redis/Nacos容器和端口，RocketMQ及业务服务未见监听。独立空库、namespace与渠道仍未核实。
- E002 completed：更新[运行验收方案](local-acceptance.md)，按实际代码区分两套Nacos入口及服务级数据库变量，补充启动与回退边界；文档完成不代表环境已获授权或就绪。
- 当前110个任务节点：**46 completed、30 pending、34 blocked**；6个人工门控仍全部awaiting_future_approval。generate继续in_progress，E003–E006不变，未执行DDL、配置发布、服务启动、真实资金操作或Git提交。
