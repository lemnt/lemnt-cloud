# 实现计划与任务草案（已确认，执行中）

未使用模版文件：项目不存在 `templates/task_template.md`。这是设计配套的任务预览；design 未确认，尚未把 task 阶段标记完成，也未开始生成业务代码。

## 文件范围

逐文件清单、操作、复杂度、前置依赖与分钟估算见 `file-manifest.md`；机器清单为 `evidence/source-files.json` 和 `evidence/new-files.json`。阶段任务和逐文件清单共同组成实施范围，不能把目录名当作“已迁完”。每行源文件仅复制到明确目标，P5 才删除原件；会员 ACL 的副本代表各上下文自己的端口，不能共享其他业务模块 Domain。

生产源码共 1,768 个 Java 文件，测试文件共 128 个；加上 XML/Lua/资源共 1,935 个源文件。上下文 ACL 的必要副本计入迁移条目，目标文件不能重名。原两个模块的 14 个 POM 和 2 个 SQL 删除动作单列在新增/修改/删除清单。

| 目标 | 迁移条目（含ACL副本） | 原业务表 | 新增消息表 |
|---|---:|---:|---:|
| product | 372 | 10 | 2 |
| pay | 298 | 11 | 2 |
| promotion | 599 | 22 | 1 |
| order | 680 | 21 | 2 |
| member | 不整体迁移 | 原17张保持 | 1 |

逐文件工时为初始估算，不是完成承诺；大型跨域事务按下表单独评估，集成/环境验证不能用文件复制耗时替代。任务完成后再填实际耗时。

## 任务依赖与验证

| 编号 | 阶段/内容 | 主要精确文件或清单 | 操作 | 复杂度 | 依赖 | 估时 | 完成验证 |
|---|---|---|---|---|---|---|---|
| G-00 | 设计、真实清单确认 | `work-items/module-split-migration/design.md`、`file-manifest.md`、`table-mapping.md` | 新增/确认 | ★★★ | 无 | 4h | 用户确认方案及逐文件预览；更新正式tasks |
| P1-00 | product六层骨架 | `lemnt-server/lemnt-product/pom.xml`及六子模块POM，清单中P1骨架文件 | 新增 | ★☆☆ | G-00 | 2h | reactor识别，保留metrics/sql启动组件 |
| P1-01 | 商品领域、数据及SQL | 逐文件清单P1 domain/infrastructure、`lemnt-server/lemnt-product/sql/lemnt_product.sql` | 新增/适配 | ★★☆ | P1-00 | 1d | 10业务表改名无遗漏，同库FK/注释保留 |
| P1-02 | 商品RPC、应用与HTTP | 逐文件清单P1 client/application/adapter | 新增/适配 | ★★☆ | P1-01 | 1d | 原商品契约/评价授权/specs保留 |
| P1-03 | 库存可靠性 | `lemnt-server/lemnt-product/lemnt-product-application/src/main/java/com/lemnt/product/application/product/service/ProductInventoryService.java`及P1 event文件 | 新增/修改 | ★★★ | P1-02 | 1d | 预占、确认、释放、回补幂等；STOCK_PRE_RESERVED同事务 |
| P1-04 | product隔离验证 | 逐文件清单P1测试、新Inbox/Outbox测试 | 新增/迁移 | ★★☆ | P1-03 | 0.5d | 仅product库可运行；商品/库存/评价测试通过 |
| P2-00 | pay六层骨架 | `lemnt-server/lemnt-pay/pom.xml`及P2骨架文件 | 新增 | ★☆☆ | P1-04 | 2h | 模块依赖无实现穿透 |
| P2-01 | 支付、钱包、退款数据 | 逐文件清单P2 domain/infrastructure、`lemnt-server/lemnt-pay/sql/lemnt_pay.sql` | 新增/适配 | ★★★ | P2-00 | 1d | 11业务表及2消息表；无order/aftersale表依赖 |
| P2-02 | 独立支付RPC与资金校验 | `lemnt-server/lemnt-pay/lemnt-pay-client/src/main/java/com/lemnt/pay/client/PaymentClient.java`、`PaymentQueryClient.java`、`WalletClient.java`及实现 | 新增/拆分 | ★★★ | P2-01 | 1d | 冻结退款请求、支付限额、原编号幂等；未知结果可查询 |
| P2-03 | 资金事实发布 | P2 event文件，`lemnt-common/lemnt-common-client/src/main/java/com/lemnt/common/client/event/CommerceEventEnvelope.java`、`CommerceEventTopics.java` | 新增/适配 | ★★★ | P2-02 | 1d | PAY_SUCCESS/REFUND_SUCCESS与支付本地事务一致；失败向上抛出 |
| P2-04 | 兼容历史任务 | `lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/payment/service/PayNotifyDispatcher.java`、P5切流说明 | 拆分/适配 | ★★★ | P2-03 | 0.5d | PAY/REFUND/AFTER_SALE分别有新归属，未决任务不能丢失 |
| P2-05 | 支付及钱包验证 | 逐文件清单P2测试 | 迁移/新增 | ★★★ | P2-04 | 1d | 渠道验签、钱包有序锁、双账本、退款重复、发送后崩溃通过 |
| P3-00 | promotion六层骨架 | `lemnt-server/lemnt-promotion/pom.xml`及P3骨架文件 | 新增 | ★☆☆ | P2-05 | 2h | 最小Client与本地层依赖 |
| P3-01 | 活动、券、购物车数据 | 清单P3 domain/infrastructure、`lemnt-server/lemnt-promotion/sql/lemnt_promotion.sql` | 新增/适配 | ★★☆ | P3-00 | 1d | 22业务表及1Inbox；不建立无事件产出的Outbox |
| P3-02 | 定价/冻结/购物车RPC | `lemnt-server/lemnt-promotion/lemnt-promotion-client/src/main/java/com/lemnt/promotion/client/PromotionCalcClient.java`、`PromotionLockClient.java`、`CartClient.java`及实现 | 新增/拆分 | ★★★ | P3-01 | 1.5d | 原资源ID锁定，同号不同内容冲突，购物车按原版本消耗 |
| P3-03 | 应用与端点拆分 | 清单P3 application/adapter；四个活动Controller移除下单方法 | 新增/拆分 | ★★★ | P3-02 | 1d | 查询/下单归属分离，原权限与模型保留 |
| P3-04 | 营销事件处理 | 清单P3 event处理器和MQ消费者 | 新增 | ★★★ | P3-03 | 1d | 券核销/释放/回退、活动名额和奖励幂等，不重扣商品库存 |
| P3-05 | 营销回归 | 清单P3测试 | 迁移/新增 | ★★★ | P3-04 | 1d | 活动/优惠券/购物车/客服/DIY测试及消费失败回滚通过 |
| P4-00 | order六层骨架 | `lemnt-server/lemnt-order/pom.xml`及P4骨架文件 | 新增 | ★☆☆ | P3-05 | 2h | order可依赖三个新Client，无实现模块依赖 |
| P4-01 | 订单与快照数据 | 清单P4 domain/infrastructure、`lemnt-server/lemnt-order/sql/lemnt_order.sql` | 新增/适配 | ★★★ | P4-00 | 1d | 21业务表及2消息表；活动订单与优惠快照各自保留 |
| P4-02 | 结算与建单 | 清单P4 settlement及PromotionOrder相关文件 | 拆分/适配 | ★★★ | P4-01 | 1.5d | 外部商品/券/活动/购物车走RPC；保留请求幂等与Redis恢复 |
| P4-03 | 售后冻结与现金退款协调 | `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/aftersale/service/AfterSaleRefundCoordinator.java`及原AfterSaleService迁入文件 | 新增/拆分 | ★★★ | P4-02 | 1.5d | 唯一处理中售后；创建时冻结；pay不写售后/数量 |
| P4-04 | 订单事件与库存确认 | P4 event处理器、InventoryGateway及订单生命周期相关文件 | 新增/适配 | ★★★ | P4-03 | 1d | 校验支付后才发布订单事实；零现金独立；库存marker不提前成功 |
| P4-05 | member冻结权益入口 | 清单member的Inbox、handler、四个Consumer、相关POM和`lemnt-server/lemnt-member/sql/lemnt_member.sql` | 新增/增量修改 | ★★★ | P4-04 | 1d | 积分/成长与Inbox同事务，失败回滚，原账本幂等保持 |
| P4-06 | 履约、分佣、自提及HTTP | 清单P4其余文件及新增四个活动下单Controller | 新增/适配 | ★★☆ | P4-04 | 1d | 发货/收货/评价/分佣/自提回归，无循环RPC |
| P4-07 | 全业务组合验证 | 清单P4测试及member新增测试 | 迁移/新增 | ★★★ | P4-05,P4-06 | 1d | 独立库+契约+消息组合测试，现金/数量/权益守恒 |
| P5-00 | 构建/配置源与实际调用方切换 | `lemnt-server/pom.xml`、清单P5调用方POM/源码、四份Nacos源模板 | 增量修改/新增 | ★★★ | P4-07 | 0.5d | 旧业务实现依赖清零；仅配置源，不发布运行环境 |
| P5-01 | 网关313端点契约 | `lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/config/ServiceRouteConfiguration.java`、对应测试、`docs/config/commerce-gateway-routes.yaml` | 增量修改/新增 | ★★★ | P5-00 | 1d | 逐方法/路径唯一目标，公共前缀不变，原其他路由保持 |
| P5-02 | SQL与架构保护工具 | `lemnt-server/sql/schema_comments.py`、`test_schema_comments.py`、release脚本、`scripts/check_module_split.py`、`test_module_split.py` | 增量修改/新增 | ★★☆ | P5-01 | 0.5d | 五个业务服务各一SQL，system不调整；旧业务表及跨上下文依赖禁止 |
| P5-03 | 清理前全量验证 | 所有相关模块及检查器 | 只读验证 | ★★★ | P5-02 | 1d | 四start+member+gateway聚合测试通过，旧入口移除预检通过 |
| P5-04 | 删除旧模块 | 清单全部源文件原路径与旧14POM、2SQL | 删除 | ★★☆ | P5-03 | 0.5d | 只删除已迁出的原件，检查git diff，无索引/提交操作 |
| P5-05 | 清理后最终回归与文档 | 当前README/边界/数据库说明；工作项`code-summary.md` | 修改/新增/验证 | ★★★ | P5-04 | 1d | 构建/架构/格式/SQL/测试再次通过；证据分层，不夸大环境验收 |

其中“P1-00”等骨架任务包括该阶段的七个 POM 与启动声明，不另生成重复的逐文件任务。清单中其他文件依赖本阶段骨架；同阶段还必须服从以上数据→契约→应用→接口→验证顺序。正式 tasks.md 将按这些编号逐项更新完成状态，不能一次批量勾选。

## DAG 与执行策略

```text
G-00
  -> P1-00 -> P1-01 -> P1-02 -> P1-03 -> P1-04
  -> P2-00 -> P2-01 -> P2-02 -> P2-03 -> P2-04 -> P2-05
  -> P3-00 -> P3-01 -> P3-02 -> P3-03 -> P3-04 -> P3-05
  -> P4-00 -> P4-01 -> P4-02 -> P4-03 -> P4-04
                                                |-> P4-05 --|
                                                |-> P4-06 --|-> P4-07
  -> P5-00 -> P5-01 -> P5-02 -> P5-03 -> P5-04 -> P5-05
```

不派生子代理。执行按原五阶段串行推进；无依赖的只读检查/测试可并行，例如 P4-05 与 P4-06 分别完成后再运行 P4-07。Maven Reactor 以实际 Client 依赖排序，不能靠书写 modules 顺序掩盖环。

过渡期 P1～P3 可能需要消费尚未迁走的旧 Client，由临时兼容 Gateway 隔离；P4 统一切换为新契约，P5 检查清零。临时改动只允许既有 POM/Gateway 或清单中的边界适配文件，不增加新业务协议。旧服务未撤下前不启动会产生同一业务副作用的新消费者/调度器。

## 验证命令与证据

先使用当前 Java17/Maven 路径，从服务聚合构建；Mockito agent和headless参数保持已有测试所需设置。测试结果保存Surefire摘要，不记录配置凭据。

```bash
python3 scripts/check_architecture.py
python3 lemnt-server/sql/test_schema_comments.py

JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  /opt/homebrew/opt/maven/bin/mvn -f lemnt-server/pom.xml \
  -pl lemnt-product/lemnt-product-start,lemnt-pay/lemnt-pay-start,lemnt-promotion/lemnt-promotion-start,lemnt-order/lemnt-order-start,lemnt-member/lemnt-member-start,lemnt-gateway -am \
  '-DargLine=-javaagent:/Users/wangxx/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar -Djava.awt.headless=true' \
  clean package
```

上面新模块命令在迁移前不可执行，不计作已通过。公共契约变化先从 `lemnt-common/pom.xml` 构建/安装所需模块，再构建 server。P1～P4 使用当前已创建模块集合做相应测试；P5 再执行全后端回归，若系统已有独立环境约束如实记录，不能因此修改 system 项目或减少原断言。

真实环境额外核对：每服务独立数据源/账号、Nacos导入/路由、Dubbo协议版本、8个Topic及隔离Consumer Group、支付验签/退款查询、Redis旧提交恢复、MQ中断/重复、部分退款端到端。未执行不得标成完成。


执行记录：用户在本预览后再次明确调用 cs-generate；方案与文件范围已确认，开始按 tasks.md 实施。以上“待确认/尚未生成”段落为确认前基线，实际进度以 tasks.md 与 code-summary.md 为准。
