# 第164批交付：订单优惠快照与按数量部分退款

代码生成完成；本地回归通过。数据库执行、部署和真实环境验收未完成。用户随后要求全项目规范批量重构，该后续范围另见第165批，不改变本批业务约定。

## 已实现

1. trade新增`trade_order_promotion`、`trade_order_item_promotion`、`trade_order_promotion_refund`，保存采用的优惠来源、真实商品行分摊、退单优惠归因。mall旧促销表继续承担活动执行和库存幂等职责。
2. 新订单记录原价、优惠、原始运费、运费减免、调价差额、行积分和已退数量；完整报价验证金额与权益守恒。历史未知保持NULL/LEGACY_PARTIAL，不能按当前规则补造历史。
3. 锁订单后按原售后order_no查询处理中记录；同订单仅一笔处理中，同requestNo校验内容后返回原退单。不记录退款中数量。
4. 创建退单时冻结现金与积分方案：10元6件退2/3/1件得到3.32/4.98/1.70元；1分钱3件可得到0/0/0.01。末次结清余额，审批、执行和重试不重算。
5. 成功退款同事务增加明细已退数量并建立权益任务；CAS防重复记账。数量与现金同时核对后才结束整单。零现金份额不伪造渠道退款记录、不向渠道发0元请求。
6. AFTER_SALE任务使用RF号、数据库时间租约和单调版本；现金成功与权益处理中/失败分别展示。新冻结RPC入口拒绝旧消费者，member按明确数量幂等返积分/回收赠积分。余额不足保留失败任务供补偿。
7. 管理端、小程序展示订单优惠及售后冻结方案；新增4个促销子资源GET，沿用原包装、权限和会员归属校验。规格继续使用specs。

## 验证证据

| 层 | 结果 | 实际边界 |
|---|---|---|
| trade | 310项通过 | 计算器、幂等、并发应用流程、生产Mapper/H2、MockMvc与序列化 |
| mall | 197项通过 | 报价快照、运费、活动分摊、冻结RPC契约 |
| member | 122项通过 | 冻结积分、幂等、余额不足回滚、旧流水兼容 |
| React | 320项/64文件通过，类型检查及构建通过 | 代码级验证；改动文件ESLint/Prettier通过 |
| 小程序 | 179项通过 | Node契约与页面逻辑；非微信原生编译 |
| SQL注释 | 98表/1100列，缺失0 | 当前初始化脚本扫描，非运行库检查 |

完整后端聚合测试与打包也通过。全量前端lint曾发现既有BannerPage测试prefer-const；全量格式检查有19个既有文件未对齐，已纳入用户后续明确授权的第165批统一重构，本批不声称当时全量lint/format通过。

测试使用JDK17、显式Mockito agent和headless AWT；全量后端MinIO本地端口测试在授权的沙箱外完成。局部日志位于`/private/tmp/lemnt164-*`、`/private/tmp/lemnt-project-backend-package.log`，机器摘要见[验证记录](evidence/order-promotion-refund-verification.json)。

## 发布准备与限制

正式新库初始化：V1..V18；已有库：`lemnt-server/lemnt-trade/sql/upgrade/order_promotion_snapshot.sql`。升级脚本需核对实际结构并在维护窗口执行一次；不得重跑初始化来升级。

历史明细refunded_quantity为NULL时阻止新售后；需核对全部成功售后、渠道退款及缺失历史后再有审计回填。旧POINT记录与PR任务保持原处理入口。

发布顺序、停旧任务执行器、新旧RPC防护、历史核对和回退限制见[迁移说明](../../docs/order-promotion-refund-migration.md)。真实MySQL、网关、Dubbo、中间件、支付渠道、浏览器与微信验收均未执行。不commit、不push。
