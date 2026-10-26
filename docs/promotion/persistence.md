# 活动版本与次数持久化 T2

需求ID：yudao-member-mall-trade-migration；promotion-v1/T2；2026-10-26（Asia/Shanghai）；责任：code-software-engineer。输入83810ab、promotion-v1设计及T1内核。

工作区维护清理了上次未推送的00b9454本地提交、缓存及证据，远端仅保留83810ab。本轮按已确认设计重建T2并重新验证，不沿用此前455项测试结论，也不声称恢复了原提交的完全相同树。

**范围：内部管理/资源事务服务和迁移文件。** 没有新增HTTP/RPC、管理页面、小程序页面、运行开关或权限授予；V1结算/支付/Outbox未接入活动计次，11个拟定API仍未实现。没有修改main或执行线上迁移。

## 实际文件和数据

复用既有模块，无新依赖：trade-domain的promotion/model和gateway/PromotionGateway；trade-application的promotion/service/PromotionAdministrationService、PromotionUsageService；trade-infrastructure的promotion/{dataobject,converter,mapper,gateway}；trade-start的promotion/PromotionPersistenceIntegrationTest。

V9__promotion_versions_usage.sql在已核实的V8后新增8表：

| 表 | 实际职责 |
| --- | --- |
| trade_promotion_root | 大小写敏感ASCII code、可编辑draft、state/revision/active/lastVersion；active指向本root实际版本 |
| trade_promotion_version | (root,version)主键、完整不可变规则/范围/名称/限额JSON、hash、算法、窗口、发布人/时间；应用仅INSERT，无UPDATE/DELETE Mapper |
| trade_promotion_scope | 静态SPU/SKU范围索引及复合版本FK，ALL不写伪造目标 |
| trade_promotion_counter | root跨版本HELD/USED及当前全局/每会员限额 |
| trade_promotion_member_counter | root/member跨版本HELD/USED；无限额也累计 |
| trade_promotion_usage | order/root唯一、订单/版本FK、会员、优惠分/hash、HELD/USED/RELEASED及时间 |
| trade_promotion_usage_event | HELD和终态的追加原因/时间审计，order/root/目标状态唯一 |
| trade_promotion_admin_request | operator/requestNo唯一、动作/内容指纹、创建/完成时间与原始回执 |

trade_order_pricing、trade_order_item_pricing、trade_order_discount仍留T4，不给历史订单补造分摊。V9只在隔离H2的V1+V9 fixture执行，其他回归保留各自V1..V8 fixture；这不是目标MySQL完整升级演练，线上未执行。

持久JSON使用明确Document和Tier字段，不启用任意类多态；未知字段/规则、损坏参数、算法/hash/窗口不一致拒绝。hash包含root/version、promo-1和完整草稿（含限额/名称），不包含实时计数。发布后的快照不随新草稿覆盖。

## 事务和幂等

内部管理服务支持create、saveDraft、publish、changeState（暂停/恢复/归档）。修改草稿不改变已发布版本；发布原子插入新版本、范围、限额及active指针。归档不可恢复/编辑，恢复不能针对已结束版本且不新建版本。未来窗口可发布但不能提前占用，过期草稿不可发布。

operator/requestNo先预占并锁行；校验动作/内容指纹并回放原回执，之后才锁root并校验expectedRevision。同key变内容/动作拒绝；已成功请求重放不因当前revision增加而失败。重复code/修订冲突/发布失败使整个事务（包括请求占位）回滚。原回执不代表当前root状态，后续界面需重新查询详情。T3仍须认证operator、检查权限和通过RPC校验范围存在性。

HELD+USED都占次数。限额挂root，发新版本、暂停/恢复不重置。无限改有限或降低限额时，不能小于root已占/已用或任一会员已占/已用。最大会员计数通过ORDER BY/LIMIT 1 FOR UPDATE读取当前值，避免旧事务快照漏掉新占用。

PromotionUsageService强制MANDATORY外层事务：后续调用者须把订单、券、usage、结算绑定及Outbox放在同一交易库事务。独立调用在写入前失败。

- hold先锁实际订单并核对本人，再锁root/global/member/usage；新占用只允许RESERVING、active且PUBLISHED、版本/hash一致并在窗口内、优惠为正。相同绑定返回原usage（包括终态），不再次扣次数或复活；变会员/版本/hash/金额拒绝。
- 全局/会员次数以有界SQL更新，任一失败回滚前面写入；非负、无long溢出、有限额度不超限。
- confirm只允许持久订单PAID/SHIPPED/COMPLETED，HELD→USED；release只允许CLOSED，HELD→RELEASED。重复同终态无写入，USED与RELEASED不相互转换。
- 已成单确认/释放不受暂停、归档、换版或到期影响。TTL/退款申请不返还次数。状态冲突保留账本并拒绝，不猜测支付结果。

内部错误码为PROMOTION_NOT_FOUND、PROMOTION_REVISION_CONFLICT、ADMIN_REQUEST_CONFLICT、PROMOTION_SNAPSHOT_STALE、PROMOTION_QUOTA_CHANGED、PROMOTION_STATE_CONFLICT、PROMOTION_LIMIT_BELOW_USAGE、PROMOTION_USAGE_CONFLICT。尚无新HTTP错误映射。

该服务不查询支付渠道，也不授权订单变成PAID/CLOSED；真实支付/原子关单核验、补偿扫描、租约/fence和worker调用仍属于T5。退款申请不能据此返券或返次数。

## 锁取舍和验证边界

当前保守使用root FOR UPDATE，再global→member→usage，所有global（包括无限额）都维护。已有订单生命周期先锁order，不反向锁会员账户；管理request→root→global，不获取order/coupon/member account。新单调用者未来须遵守member/coupon→root整体顺序。此实现会串行化同一活动写入，未做吞吐/热点容量声明；共享锁优化需目标MySQL并发证据。

锁查询显式刷新并绕过MyBatis缓存，确保同一事务改变订单状态后重新读到实际状态。数据库epoch采用UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0精确十进制乘法，避免H2 SIGNED映射32位溢出。H2 MySQL模式原生运行Mapper及V9 SQL，不替换SQL。DDL连接保留到测试结束，避免H2 CHECK常量集合比较持有已关闭会话；约束没有关闭。一个窗口/晚回调测试显式前移Gateway时钟，不sleep、不改发布快照；普通用例读实际数据库时间。

24项真实SQL/事务测试覆盖版本/hash/范围、原始回执和修订、全局/会员最后额度、跨版本限额、降额与占用竞争、暂停/恢复/归档/到期、重复终态及支付/关闭互斥、后续写入和审计失败的回滚、外层事务要求、损坏JSON/hash、CHECK/FK。**本次恢复后完整根Maven verify PASS：71项目、455项测试、0失败/错误/跳过**。新增24项；T1和原回归均保留通过。证据及实际命令见[验证目录](../../work-items/yudao-member-mall-trade-migration/evidence/promotion-persistence/)，含测试名、输出摘要和23个新源码/SQL哈希；不是沿用历史结果。

H2不是目标MySQL锁/排序规则/时间精度验收。MySQL/Redis/Nacos/Dubbo/支付渠道、浏览器/原生编译/设备仍NOT RUN，React/小程序未修改、未重跑。下一步T3权威事实RPC和管理API；T4 V2缓存提单、T5生命周期/补偿、T6页面和T7真实集成待实现。回滚保留版本/计数/usage/审计，未来V2订单存在后保留支持这些资源的worker。PR保持Draft，不宣称生产可用。


后续T3事实RPC及六个管理HTTP入口已实现，最新状态与证据见[management.md](management.md)。本页原阶段测试数量和限制保留为历史，不代表后续阶段仍未实现。
