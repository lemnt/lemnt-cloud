# F02 售后标准表、操作日志和本人取消

2026-10-28 v1，责任code-software-engineer。承接27完整迁移设计、29订单日志设计及30交付记录；参考同Skill开发设计/接口文档/Java示例。输入为本地芋道AfterSaleDO、AfterSaleLogDO、AfterSaleStatusEnum、AfterSaleOperateTypeEnum、AfterSaleServiceImpl、Vue售后管理及uni-app售后调用。沿用system六层、app/web之后分域、逐表Gateway/DO/BaseMapper、简单Lambda及复杂XML/resultMap；不提交代码、不运行真实DDL。

## 现状、目标与边界

当前trade_refund_request以refund_no为主键，售后表组件混在fulfillment，Mapper含重复CRUD及注解SQL，售后状态无日志。现有部分退款、发货后售后、退货退款、同步渠道边界和订单锁额度校验继续保留。

本批把表改为trade_after_sale，增加真实自增id、order_id、审计字段及refund_time，业务单号保留唯一索引以兼容原支付退款关联；组件迁到aftersale域并命名AfterSaleGateway/DO/Mapper。领域和响应补id/orderId/refundTime，不以字符串单号伪装源项目数值ID。历史order_id可根据订单号准确回填；不能恢复的审批人、退款时间不伪造。

新增trade_after_sale_log，关联真实after_sale_id，字段user_id/user_type/before_status/after_status/operate_type/content及BaseDO审计/逻辑删除。源状态映射：REQUESTED10、AWAITING_RETURN20、RETURNED30、WAIT_REFUND与当前内部REFUNDING40、REFUNDED50、CANCELLED61、REJECTED62、RETURN_REJECTED63。操作10申请、11同意、12拒绝、20寄回、21收货、22拒收、30发起退款、31成功、32失败、40会员取消。系统(0,0)、会员(实际id,1)、后台(认证id,2)，不能把后台处理伪装成会员。

日志与售后/退款状态同事务，重复状态操作不重复写日志。渠道调用继续在事务外，退款成功才写refund_time；WAITING不得冒充成功，失败记录实际失败且重复结果不重复日志。真实SDK继续后置。

补本人取消：与芋道相同，仅申请/同意退货/买家已寄回阶段可取消，禁止等待退款、退款中和已完成取消。会员编号来自网关上下文，行锁下校验归属，即使重放也必须校验。取消释放商品售后额度，后台取消不新增额外入口。

后台日志GET /trade/admin/after-sales/{no}/logs，复用trade:after-sale:query；本人取消POST /trade/app/after-sales/{no}/cancel。独立Req/Res、中文Schema及统一异常，时间数据库同一时区；金额仍为项目已确认的元/BigDecimal，不能悄然把元当分。后台详情展示真实日志，小程序对接本人取消。

## 来源字段缺口

完整商品SPU/规格/图片尚未全部进入下单商品快照，本批不通过查询当前商品伪造历史售后快照。申请描述/凭证、完整商品快照、pay_refund_id数值关联和其余source查询仍按完整迁移继续；本批不能宣称AfterSaleDO所有源字段已对齐。

## 任务与验收

1. 新库DDL和独立升级SQL、标准表/域/主键、Mapper精简 → H2生产Mapper持久化、旧数据升级不丢业务单号关联。
2. 日志应用编排及审批/退货/退款接入 → 各操作顺序、操作者、源状态；重复调用不重复、日志写失败回滚状态。
3. 本人取消、接口和后台日志页面 → 归属/状态、取消后额度恢复、等待退款拒绝取消、前端错误/空数据。
4. 三模块回归、React类型/测试/构建及小程序测试 → 证据区分H2本地事务、受控RPC/消息/渠道、真实环境未运行。

旧库升级必须先停相关写入、备份并审核SQL；物理表名和主键变更不支持直接切回旧服务。源支付退款外键须重建到新表唯一业务单号，日志不回填虚构历史。SQL只生成不执行真实库。
