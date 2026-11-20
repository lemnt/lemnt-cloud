# F02 售后标准表与日志交付记录

2026-10-28，code-software-engineer；设计31-aftersale-log-design.md，承接27完整迁移清单。保留原工作区未提交改动，迁移前源码备份/tmp/lemnt-aftersale-before.tar。本批未提交、推送、部署或执行真实数据库变更。

## 已实现

- trade_refund_request迁为trade_after_sale，自增id与唯一refund_no分离，增加order_id、audit_user_id、refund_time及BaseDO审计/逻辑删除。实体命名AfterSaleDO，Gateway/Mapper/Converter及领域模型迁到aftersale，按表拆分。
- 删除原Gateway未使用的整单退款写入/驳回/重复分页方法及Mapper重复CRUD；简单操作改MyBatis-Plus Lambda，行锁SQL进入AfterSaleMapper.xml，包含明确resultMap，锁读flushCache=true/useCache=false。
- 新增trade_after_sale_log及逐表组件；申请、同意、拒绝、寄回、收货、拒收、发起退款、退款成功/失败和取消按源状态/操作类型记录。日志关联真实数值ID，后台实际操作者从认证上下文传入；回调为系统，不将后台处理冒充会员。
- 申请/审批/退货与日志同事务；发起退款/结果确认与日志同事务，渠道调用仍在事务外。审批日志失败回滚状态；重复操作、成功/失败重放不重复日志。只有退款成功写refundTime；审批身份只写真实后台人员，历史不填虚假0号管理员。
- 本人取消POST /trade/app/after-sales/{no}/cancel：行锁内核对归属，重复取消也核对；申请、同意退货、已寄回可取消，等待/处理中退款不可取消；取消释放商品售后额度，不退款或归还订单库存。
- 后台补详情、同意、驳回、确认退货收货、拒收、原路退款/核验与日志展示；独立权限控制。WAITING只提示受理，SUCCESS才提示退款成功，FAILED明确尚未完成售后。小程序仅新增取消售后客户端传输，未将完整App售后页面标为完成。

## 验证

PASS：mall/trade/member及依赖260项后端测试，clean verify及最终verify通过；包括真实H2生产Mapper/XML/本地事务、完整退货退款日志、重复操作/回调、日志失败回滚、取消归属和额度恢复、退款阶段禁止取消、HTTP伪造会员输入隔离。RPC/MQ/退款渠道仍为受控测试边界。

PASS：React166项测试、类型检查/构建、lint、Prettier；小程序81项Node测试。机器证据evidence/aftersale-log-verification.json。

NOT RUN：真实MySQL升级、真实Redis/Dubbo/MQ传输、真实渠道SDK、浏览器和真机端到端。初始化与升级SQL已生成，不代表已执行；参数/日志样例不冒充线上HTTP实测。

## 数据升级及风险

新库用V5/V6/V9。旧库须已具备上一版售后与支付退款表，停相关写入备份后审核after_sale_identity_and_log.sql，再创建V9日志表；不能混跑新初始化与旧升级。业务单号及支付退款关联保留，订单ID可按原唯一订单号回填；创建时刻取原申请事实，退款时间仅取唯一成功退款记录的本地成功落库时间；未知历史审批人、最后修改时间和操作日志不伪造。主键及物理表变更不支持直接热切回旧服务。

## 尚未完成

本批并非完整源售后字段复刻：申请描述/凭证、原订单完整SPU/规格/图片快照、售中/售后类型、pay_refund_id/logistics_id数值关联、独立receive_reason和其他源查询仍需迁移。当前金额元/BigDecimal、状态字符串及部分字段别名适配仍存在，明确记录在evidence/after-sale-fields-20261028.json，不查询当前商品假装历史快照。

后台代申请/代登记退货的旧适配仍需在完整App申请/寄回页面迁移后收敛；当前没有恢复旧整单申请入口。完整App售后列表/详情/申请/退货页面尚未迁移，不能据客户端取消方法声称App端完成。

当前初始化46张表，仍有36张业务表无对应表（另2演示不迁）。F02及F03-F12继续，尤其订单完整快照/字段、调价/改址/评价、活动、钱包、分销、内容客服与统计。source清单保持in_progress/pending。

接口说明：docs/after-sale-log-api.md；原接口文档已指向当前变更。下一步先补原订单商品快照，再补售后申请资料和完整App售后交互，避免字段有了却没有可信供数。
