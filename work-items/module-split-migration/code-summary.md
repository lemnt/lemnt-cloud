# 四模块拆分代码进度

P1～P4 与 P5-00～05 全部完成：四模块代码、网关契约、旧模块清理及清理后全量回归均已交付。真实环境迁库、Nacos发布、MQ/Dubbo网络与在线切流仍未执行。

## P1 product

六层商品服务、独立10张业务表与Outbox/Inbox已迁入。P4已把商品评价的订单调用从旧trade-client切换到order-client；订单支付/关单/售后事件在商品本库处理库存并登记Inbox。阶段首次验证93项，后续增加Mapper和消费用例后的最新结果见下表。

## P2 pay

支付/退款/钱包/充值与配置HTTP已迁入，11业务表+Outbox/Inbox独立建表。资金成功与资金事件同事务，零现金不创建退款交易。P4补充支付准备凭证、关单围栏、佣金钱包原审核单核验；未知结果按原编号重试。历史未决通知仍须切流前审核。阶段首次验证107项，最新结果见下表。

## P3 promotion

22张业务表与Inbox独立建表。报价、券/活动冻结、购物车消费经明确Client传输，资源串行与关单围栏防止延迟重扣。P4补充重复SKU预占数量合并校验，券计价领域测试随职责移动到promotion，保留原断言。阶段首次验证73项，最新结果见下表。

## P4 order：核心实现与完整组合回归

已实现订单六层、21张业务表与Outbox/Inbox；结算引用本地订单Provider，商品、营销、支付、会员经独立Client衔接。直接提单的新测试覆盖类型明确的远程报价、原规格ID/名称落库、请求重试复用原订单。商品评价也已切换新订单Client。

- 支付准备凭证在订单锁内登记，远端创建响应未知时仍阻止调价；支付库通过稳定围栏阻止关单后的延迟支付创建。关单不会在订单库更新资金表，真实迟到资金保留给对账。
- 订单核验真实资金成功后，同事务提交PAID及ORDER_PAID；零现金只产生订单业务事实。只有商品、营销、会员实际消费回执都满足后才写库存完成marker，并与佣金入账同一订单事务。
- 售后创建时冻结现金、数量及积分；只允许一笔处理中售后。历史优惠或积分不完整时阻止新申请，不补造历史。现金完成经资金消息或原请求成功查询走同一核验，成功数量、售后状态及AFTER_SALE_COMPLETED同事务。
- 保留固定单件退款及最后尾差：10.00/6按数量2、3、1为3.32、4.98、1.70；0.01/3前两份为0，最后一份0.01。全退同时检查现金和全部商品数量。重投原售后事件保持原载荷及原全退标志。
- 商品、营销、会员各自在本库登记Inbox及权益效果，失败回滚凭证。权益详情只把真实消费回执展示为成功，历史缺失展示UNKNOWN。
- 佣金提现先持久化审核通过状态10，再在订单事务外请求钱包，最后提交状态11。钱包以原BW编号幂等入账，响应丢失保留10供恢复；一旦开始入账不能驳回返佣金。发货、收货、自提状态事务登记对应订单事件。
- 有界恢复扫描支付后处理、退款执行及审核通过的提现；需在切流审核后显式启用。旧补偿Topic消费者默认关闭，正式事实仍为约定8个Topic。

验证中修复：锁单只返回头导致事件缺失商品行；售后锁ResultMap遗漏五个冻结字段；恢复查询引用订单表不存在的deleted字段；类型规范工具误把java.lang.reflect子包当作自动导入；支付准备凭证使用固定字段顺序避免不同JVM的Map顺序造成伪冲突。已分别补充生产映射或工具回归。

最新证据：evidence/p4-order-combination-tests.json。此前核心分层阶段记录保留在evidence/p4-order-core-tests.json。

| 范围 | 测试数 | 失败/错误/跳过 |
|---|---:|---|
| order（含start） | 302 | 0 / 0 / 0 |
| member（含start） | 125 | 0 / 0 / 0 |
| product（含start） | 97 | 0 / 0 / 0 |
| promotion（含start） | 78 | 0 / 0 / 0 |
| pay（含start） | 114 | 0 / 0 / 0 |

五个服务不跳过测试的clean package通过，共716项，失败、错误、跳过均0。最终夹具清理后再次对order-start及依赖执行完整package，编译、测试、打包均通过。Java架构检查0违规，格式工具9项回归通过，变更Java按固定版本AOSP格式及显式import检查通过。

P4-07将原普通结算106项、活动订单9项组合场景接到四套独立H2数据库和生产组件；生产Client Provider经测试RPC传输跨线程调用，不能共享调用方事务。生产Outbox租约、原信封和Inbox效果保留，消息消费失败后重投原事件。会员资料/余额与回执、外部支付渠道是边界替身；真实member权益、账本另有125项独立回归。HTTP契约由实际新模块Controller聚合核对原路径、方法和权限。

组合回归补齐支付日志类型10和退款失败日志类型32：失败退款核验原资金记录和冻结方案，在订单锁内幂等追加日志；日志写入失败保留资金库结果。拆库后资金成功不随订单侧失败回滚；库存和券分别补偿，按原事件恢复，重试不重复归还库存或更新已退数量。原106+9项场景未删除或禁用；原跨服务同事务断言已按批准的事件驱动设计改为独立事务及最终恢复检查，并补充资金事实保留、回执缺失和原计划冻结断言。

**尚未执行：** 真实MySQL迁移、Nacos发布、MQ/Dubbo网络及真实网关HTTP验收。旧mall/trade保留；P5-00构建/配置与调用方检查已完成（见下文），313端点网关契约已完成（见P5-01），后续保护工具及清理未开始，整体迁移仍在进行中。

## P5-00 构建、配置源与实际调用方

默认server Reactor移除旧mall/trade构建项，原目录保留到P5-04；全部59项validate通过。其余生产/测试源码与POM的旧包及旧依赖检查清零，实际Client调用方已在P1～P4完成切换，本批不重复改写正确调用。五个启动JAR再次核对：没有旧mall/trade库，也没有其他上下文的Domain/Application/Infrastructure/Adapter实现；跨服务仅Client。

补齐四份独立数据源/Dubbo Nacos模板与共享commerce-mq模板，五个服务启动配置导入共享MQ。15个正式消费者与3个Outbox Relay必须显式启用，默认或false均不装配副作用组件；真实Spring条件装配和模板缺凭据/占位符测试通过。保留原Redis恢复键、支付应用标识、调度配置键与公共路径；历史补偿配置只作原任务审核参考，不作为新事件配置源。配置归属、8Topic/15消费组、停写/数据核对/未决任务和回滚边界见docs/module-split-cutover.md。

最新证据：evidence/p5-build-config-tests.json。五服务不跳过测试的clean package通过，共721项：order304、member126、product98、promotion78、pay115，失败/错误/跳过均0。架构0违规，格式工具9项通过，本批22个Java文件格式及显式import检查通过。

此证据只覆盖配置源与本地装配/回归，没有发布Nacos、执行真实库迁移、创建Topic、启动服务或切流。该阶段当时网关旧路由尚待P5-01适配；后续内置路由映射已完成（见下文），在线配置未切换，不得直接撤下运行中的旧服务；P5-02保护检查、P5-03清理前全量验证、P5-04删除及P5-05最终回归仍未执行。未commit或push。

## P5-01 网关端点映射与兼容

替换Java内置mall/trade粗路由，按原313个HTTP方法/路径转发到product58、promotion122、order86、pay47个目标。唯一标准源docs/config/commerce-gateway-routes.yaml原样打包，使用Spring Binder/RouteDefinitionRouteLocator加载；16个业务路由组加4个OPTIONS组，GET保留隐式HEAD。路由保持原前缀与查询编码，不StripPrefix或改写请求体；内置路由隔离动态配置的默认改写过滤器，仍执行网关既有全局过滤器。

313个端点逐一执行真实网关谓词，唯一匹配对应新服务；四个新模块的真实Controller完整映射与冻结的方法/路径/权限/归属清单一致。业务契约验证放在已有order-start测试装配中，网关未引入业务模块或Dubbo依赖。保留既有会员认证优先级、member/auth/system路径及过滤行为；CORS预检、HEAD/OPTIONS、未知方法/路径和默认改写配置场景通过。未修改认证、CORS和Sentinel源或阈值。

证据：evidence/p5-gateway-route-tests.json及evidence/gateway-endpoint-contract.json。网关、order-start与实际Reactor依赖不跳过测试的clean package通过，最终源码package再次通过；本批共766项（网关350、order305及其他依赖111），失败/错误/跳过均0。此范围不包括其他服务start或真实跨服务HTTP，不与P5-00的五服务721项相加。架构0违规，格式工具9项通过，3个Java格式/显式import检查通过。

网关JAR核对路由源仅一份且字节与源一致，原application.yaml保留并完成profile过滤；没有Dubbo或业务实现库，冻结测试清单不进入生产包。旧mall/trade路由示例已标为停用记录。不要把内置清单再合入Nacos，发布前需审核并移除在线旧粗路由及重复定义、核对部署环境自定义Sentinel资源映射，保留原阈值/窗口和其他服务配置。

本批没有发布Nacos、操作在线路由、部署或切流，没有删除旧模块或commit/push。P5-02保护工具、P5-03全量验证及P5-04～05清理/最终回归仍待执行。

## 用户补充：类型导入规范

所有Java代码类型使用短名及显式import，覆盖Serializable、Map、跨行引用和静态调用。字符串/反射类名/XML按协议保留。system只同步此前的纯Java类型规范，本批不调整system业务或SQL。

## P5-02～P5-04 补记

`scripts/check_module_split.py` 提供模块与表归属保护（当前 product12/promotion23/order23/pay13，共71表，违规0）；旧 mall/trade 目录与初始化SQL在提交 af77e846 删除，默认 Reactor 已无该两项。这三项在 tasks.md 中已勾选，但工作项内没有对应批次的独立测试证据文件，其结果由 P5-05 的仓库级全量回归覆盖并重新归因。

## P5-05 清理后全量回归

在 HEAD a628d381（旧模块已删除、工作树干净）上重跑整链构建：`lemnt-dependencies` install、`lemnt-common` 带测试 install、`lemnt-server` 全 Reactor 不跳测试 clean package，三段均 BUILD SUCCESS，60个Reactor模块、MVN_EXIT=0。

| 范围 | 测试数 | 失败/错误/跳过 |
|---|---:|---|
| gateway | 350 | 0 / 0 / 0 |
| order | 305 | 0 / 0 / 0 |
| system | 147 | 0 / 0 / 0 |
| member | 126 | 0 / 0 / 0 |
| pay | 115 | 0 / 0 / 0 |
| product | 98 | 0 / 0 / 0 |
| promotion | 78 | 0 / 0 / 0 |
| auth | 20 | 0 / 0 / 0 |
| common + open | 3 | 0 / 0 / 0 |

合计 **1242 项，失败、错误、跳过均为 0**。范围含既有 system/auth/gateway/open，与 P4 的716项、P5-00 的721项口径不同，不相加。`check_architecture.py` 3042个Java主源、68个Mapper XML、86个POM，违规0；`check_module_split.py` 违规0；三个检查工具自测 7/11/9 项全部通过。证据见 evidence/p5-full-regression.json。

回归中发现并修复：`scripts/test_architecture.py` 的 record 夹具原先以全限定注解和 `java.util.List` 书写，被后续新增的 qualified-type 规则命中两条，导致 `assertEqual(['http-field-schema'])` 失败。夹具改为显式import短名，断言与覆盖面不变，未删除或放宽测试。该修复被仓库提交 12671995 连带入库，本工作项未执行 commit。

仓库级格式检查不干净，且**不属本批修复范围**：固定版本 gjf 判定 2470/3286 个Java文件需重排，import 分组顺序差异 287 个，分布于 product53、system52、promotion49、order48、member39、pay27、common/security/gateway/auth 共28个。差异全部仅为导入顺序，且同样出现在迁移未触碰的 system/auth/gateway/lemnt-common，属仓库既有状态——历史各批只检查当批变更文件，从未做过仓库级检查。未执行 `--write`，未对无关文件重排；如需统一，应作为独立工作项评审。

回归中另记录一个待决设计问题：product_message_outbox 与 product_message_inbox 列、键与方向均不同（前者14列含投递状态机与租约，后者6列仅消费幂等凭证），不构成互相重复；但 product 发布 STOCK_PRE_RESERVED 后，唯一消费方 promotion 的 `StockPreReservedEventHandler` 不改任何业务状态、只做交叉校验，且 product 侧 `confirmStock` 在 CONFIRMED 状态已自带重复投递防护。事件链路冗余与 inbox/状态机重叠两项是否调整，待确认后另立工作项，本批未删表、未改契约。

**尚未执行：** 真实MySQL建库与迁库、Nacos发布与在线旧粗路由撤除、MQ Topic与Dubbo网络及Outbox/Relay真实投递、网关在线切流与端到端HTTP验收；lemnt-ui 与 lemnt-miniapp 未运行（本工作项无前端改动，且依赖未安装）。未 commit、未 push。
