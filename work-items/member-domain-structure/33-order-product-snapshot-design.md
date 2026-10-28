# F02 原订单商品快照贯通

2026-10-28 v1，code-software-engineer。承接27完整迁移、31售后设计及32交付；参考Skill development-design/api-documentation/Java/TypeScript规范与项目AGENTS，以及本地芋道TradeOrderItemDO、AfterSaleDO及其商品属性快照。沿用现有React与小程序，不提交/推送、不执行真实DDL。

## 输入和实际缺口

mall已有SKU/SPU及SKU规格名值表，结算商品快照目前只带SKU编码/名称/价格及配送参数，交易明细缺SPU、规格与图片。源项目在订单项保存spu_id/spu_name/sku_id/properties/pic_url，售后从订单项复制，不反查当前商品。

当前SKU规格关系只存属性名称和值，没有源项目propertyId/valueId。此批保存实际名称和值Map，不通过名称临时拼凑字典ID；结构化规格ID关系随商品域继续迁移。历史未保存字段保持NULL，不从当前目录回填；已知无规格使用空Map，与历史未知区分。

## 方案和边界

1. 目录批量读取SKU/SPU与规格名值；ProductSnapshot扩展spuId/spuName/properties/picUrl。图片按SKU优先、为空取SPU已有图片，均无值则为空，不填虚构占位URL。沿用ProductSkuSpecGateway批量读取，避免逐行查库。
2. 商品元数据通过SettlementItem→计价与运费分摊→Redis结算→OrderCreateBo/OrderItemDTO→TradeOrderItem贯通。各层独立数据契约，不把领域对象直接作跨服务DTO或HTTP Res。报价指纹纳入排序后的名值及长度编码，改名/规格/图片后旧报价拒绝提交；商品改动后已落库订单保持原快照。
3. trade_order_item增加spu_id/spu_name/properties/pic_url，现有product_id明确改存sku_id，Java及客户端productId暂作为既有SKU别名保留，通过@TableField映射，避免同时存两份SKU编号。保存/读取使用按表BaseMapper、单独Converter隔离JSON；XML如新增必须有resultMap。
4. 可售后商品额度从OrderRefundItem返回原快照；创建售后复制该行spuId、productId(SKU)、spuName、properties和picUrl到trade_after_sale。审批、取消与退款不改写快照，售后不调用mall现值补历史。
5. 管理订单详情、售后详情/可申请商品响应增加实际快照字段，独立Req/Res保留；React显示原SPU/规格/图片，未知显示未记录，不把缺值当无规格。仅HTTP输出新增，客户端不提交这些字段。
6. Redis报价键从mall:settlement:v2迁到v3，使旧预览重新报价；既有提交/幂等回执前缀mall:{settlement}:v2保留，历史提单重试不主动补新目录资料。RPC兼容旧快照为空，但当前完整商品目录发出的新报价带实际元数据。价格、积分、配送和下单同步远程边界保持原规则。

## 任务与验证

- 合同与目录来源：修改mall product/settlement各层及trade-client，检查全部生产构造/拷贝调用，不能依赖兼容构造器静默丢字段。
- 订单/售后存储：按表DO/Mapper/Gateway、Converter与初始化/独立升级SQL；历史只改SKU列名，不伪造SPU/规格/图。
- 应用和界面：原订单详情、售后额度与创建快照、HTTP Converter、React解码/展示，小程序兼容额外输出字段。
- 验收：真实双库链路中下单后修改商品名/规格/图，订单与售后仍保留原值；报价前后改商品即409；变元数据的同订单号重放被拒；积分/优惠/运费拷贝不丢字段；缺失历史字段仍NULL。运行三模块回归、Redis序列化、独立HTTP契约和前端检查。

source属性ID、原订单其余金额拆分/状态、源售后申请资料等仍未全量复刻。本批无新表，不改变36张业务缺表总数，不以快照字段完成宣称全量商城迁移完成。真实MySQL/Dubbo/Redis/MQ/渠道及浏览器真机端到端单独标记未运行；升级主键/表名依31批次先完成，SKU列改名需停旧写者并审核SQL后执行，不能新旧写入混跑。
