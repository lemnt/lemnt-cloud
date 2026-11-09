# 本地隔离运行与验收方案

> 2026-11-06设计方案；2026-11-09补充[只读环境预检](local-environment-preflight.md)。初检Docker不可达，复核时MySQL/Redis/Nacos容器及端口已出现；但隔离资源及操作范围尚未批准，RocketMQ/业务服务未见监听。**本次未执行基础设施准备、服务启动和真实业务验收**。自动化复核结果另存evidence，不以测试通过证明业务已跑通。
> 此方案只针对当前lemnt-cloud；不运行本地芋道参考源码的构建、安装脚本或SQL。

## 1. 环境门控

在cs-task/实施前记录：本机中间件是否存在、版本、端口冲突、独立数据目录、可操作权限。禁止从历史会话或默认配置推断已有权限。

推荐使用专用本地实例/容器和单独配置；若复用本机服务，必须确认它不是现有业务环境并划定库/命名空间。Docker不是强制前提，不为迁移默认重建全部中间件。

| 组件 | 隔离要求 | 本阶段状态 |
|---|---|---|
| MySQL | 独立空库；各服务数据源独立，不混库掩盖跨域访问 | 复核已见mysql8容器和3306监听；实际版本/独立空库未核实，未建库、未执行DDL |
| Redis | 专用实例或确认过隔离策略，避免污染现有会话/结算键 | 复核已见redis7容器和6379监听；实际版本/隔离策略未核实 |
| Nacos | 当前兼容版本、独立namespace/group；数据ID与实际启动导入一致 | 复核已见Nacos v3.2.1镜像及8848/9848监听；实际健康/空间未核实，未发布 |
| RocketMQ | 与当前客户端匹配；确认普通/事务消息、Topic/组/权限及回查配置 | 9876/10911未见监听；版本/订阅未核实，未启动或创建Topic |
| 文件/物流等 | 按实际能力需要明确本地替身与真实服务 | 未核实；替身不等于真实渠道验收 |
| 微信工具/渠道 | AppID、允许访问域名、HTTPS、支付/退款沙箱、回调可达 | 待用户提供或确认条件 |

库名建议统一使用专用前缀，例如`lemnt_migration_local_product`等；这只是命名建议，不授权创建、改写已有库或数据库用户。

## 2. 配置清单（不得填秘密到版本库）

| 类别 | 项 | 处理 |
|---|---|---|
| 运行时 | Java17、项目Maven/Node工具链 | 依据当前pom/package，不升级来解决迁移逻辑 |
| 数据源 | 四个拆分服务分别使用`{PRODUCT\|PROMOTION\|ORDER\|PAY}_DB_URL`、`{服务}_DB_USERNAME`、`{服务}_DB_PASSWORD` | 对应独立空库；member/auth/system实际配置另核对，不假定通用DB_USERNAME生效 |
| 注册配置 | 拆分服务使用NACOS_SERVER_ADDR/NACOS_NAMESPACE/NACOS_GROUP；gateway/auth/system/member使用NACOS_HOST/NACOS_PORT、构建profile namespace及DEFAULT_GROUP | 两套入口必须显式对齐同一批准实例/隔离空间；仅设置NACOS_NAMESPACE不能覆盖所有服务，详见只读预检 |
| Dubbo | 服务协议端口、注册namespace/group | 使用现有服务端口变量，避免重复占用 |
| 安全 | SECURITY_TOKEN_SECRET等 | 遵守既有HS512长度约束；auth/gateway同一安全配置；不记录值 |
| MQ | ROCKETMQ_NAME_SERVER、生产者组、Topic/consumer-group | 不仅配置连接，还要核对事务监听器/回查与订阅 |
| 副作用开关 | COMMERCE_MQ_ENABLED及各现有任务/兼容开关 | 审核后仅在独立验收环境开启必要组件；旧任务入口不因方便跑通自动开启 |
| 管理端 | VITE_APP_BASE_API | 现有默认`/dev-api`，Vite转发网关，不误连生产 |
| 小程序 | apiBaseUrl、memberApiReady、paymentAppKey | 现有配置为空/false；按验收环境显式设置，不自动开启生产入口 |

当前来源：`docs/config/lemnt-product.yaml:L4-L14`；`docs/config/commerce-mq.yaml:L4-L10`；`lemnt-miniapp/config.js:L1-L3`；`lemnt-ui/src/api/interceptor.ts:L6`；`lemnt-ui/vite.config.ts:L17-L21`。

## 3. 服务启动拓扑

当前HTTP默认端口（实施时检查实际可用性）：

| 服务 | 端口 | 就绪证据 |
|---|---:|---|
| gateway | 8080 | 路由加载、匿名读/会员/管理身份规则正确 |
| auth | 9200 | 管理与会员登录会话签发正确、依赖就绪 |
| system | 9100 | 管理登录所需账号/菜单/权限/参数可用 |
| member | 9110 | 会员查询、会话依赖及商城所需账户规则 |
| product | 9140 | 商品读写/库存、公开读与可信会员请求 |
| promotion | 9150 | 购物车/券/活动及必要资源规则 |
| order | 9160 | 结算/建单/履约/关单/评价 |
| pay | 9170 | 沙箱或明确替身的支付/退款；真实结果与替身分开 |
| React开发入口 | 5173 | 浏览器经网关登录和操作，不只静态渲染 |

步骤：基础设施及独立空库准备 → 核对并加载配置 → system/member等依赖 → auth/product/promotion/pay/order分别启动 → 检查实际Dubbo注册与调用 → gateway → React/小程序。存在循环运行依赖时可并行启动provider再等待全部注册；不通过`check=false`误称服务调用已成功。

HTTP开发服务使用项目preview启动入口；非HTTP长进程使用用户可见终端。不得在设计阶段偷偷启动或发布；真实启动命令和环境模板在实施时固定并记录退出/就绪状态。

## 4. 自动化与构建验证（实施时执行）

从当前项目根目录，在确认本机环境后依序执行；下面是验收命令，不代表已经运行。

```bash
mvn -f lemnt-dependencies/pom.xml install
```

```bash
mvn -f lemnt-common/pom.xml install
```

```bash
mvn -f lemnt-server/pom.xml clean package
```

```bash
python3 scripts/check_architecture.py
```

```bash
python3 scripts/check_module_split.py
```

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
```

在lemnt-ui目录执行：

```bash
yarn typecheck
```

```bash
yarn test
```

```bash
yarn build
```

在lemnt-miniapp目录执行：

```bash
npm test
```

格式检查使用项目现有固定版本工具；只格式化当批修改范围。每条命令记录真实退出码，不通过`grep | head`的成功覆盖Maven失败，不跳过失败测试、不批量删除target以外文件或改Git索引。

## 5. 实际业务场景

| ID | 操作 | 必须核验 | 外部依赖缺失时 |
|---|---|---|---|
| L01 | 管理员登录、配置分类/品牌/规格/商品并发布 | 页面、权限、DB字段、公开浏览一致 | 阻塞，不用手工拼DB代替管理功能验收 |
| L02 | 匿名商品分页/详情/评价；分类列表 | 新路径公开可读；排序/筛选/字段；匿名评价脱敏 | HTTP/浏览器分别记录，Node mock不替代 |
| L03 | 会员登录后收藏、足迹、购物车 | 会员身份正确、错身份/伪造头拒绝；业务结果归本人 | 微信工具缺失则小程序平台验收未完成 |
| L04 | 原会员结算→建单→支付结果 | 金额、优惠、SKU/活动资源及订单状态；重复请求无额外效果 | 替身可验证内部链，不得记真实渠道通过 |
| L05 | 未支付关单/超时及迟到支付 | 支付不明不当失败；资源回退/关闭结果可核验 | 相关渠道或消息路径缺失标阻塞 |
| L06 | 发货/自提等源履约方式→完成→评价 | 真实订单项归属、状态/日志；查询product、投稿order | 物流/设备/渠道依赖按实际能力分列 |
| L07 | 按项售后→审核→退款→库存/权益处理 | 原冻结金额和件数，重复结果、乱序、失败恢复 | 缺退款沙箱时标未验收 |
| L08 | 内容/统计及其余营销、会员依赖能力 | 按capability-matrix逐方法与页面核对 | 不因核心闭环成功而跳过 |

## 6. 消息与故障演练

只在可丢弃的本地环境执行：重复相同事件/同业务键新事件号、先取消后预占、支付确认与售后乱序、DB暂不可查、broker不可用、本地事务提交后二阶段应答中断、支付/关单竞争。

每条记录事件号/业务号/traceId、操作时间、消息状态、原业务记录及最终结果，秘密和个人资料脱敏。验证业务结果而非仅消费日志出现。测试期间不得操作已有消息积压、删除真实业务行或清空共享Redis。

## 7. 证据格式与完成判定

后续每批生成`evidence/acceptance-<批次>.json`或同等可读报告，至少包含：

- 日期、代码HEAD/工作区状态、源文件指纹、环境名称（非凭据）。
- 命令/操作步骤、预期、真实退出码/HTTP状态/业务结果。
- 数据核验（所属服务、原业务号、关键字段前后变化）与截图/网络/日志证据路径。
- `static_verified / automated_passed / local_runtime_passed / external_sandbox_passed / blocked / not_run`分别记录。
- 不可执行的真实条件、原因、负责人和下一步；不能用空白默认为通过。

**总体判定**：未完成真实基础设施和页面操作，只能叫自动化通过；外部渠道缺条件只能叫该项未验收；全量矩阵有未核实/未实施项时不能叫商城迁移完成。本设计阶段以上状态均未运行。
