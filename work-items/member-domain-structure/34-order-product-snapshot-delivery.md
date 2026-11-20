# 原订单商品快照交付

2026-10-28，承接33设计。本批完成快照纵向链路，不代表商城全量迁移完成。

## 已实现

- 商品目录批量读取SPU、SKU及真实规格名值；采用SKU图片，空时取现有SPU图片。
- ProductSnapshot/SettlementItem贯通促销、会员折扣、优惠券、积分、运费分摊、Redis结算及跨服务订单DTO，所有生产拷贝传递商品元数据。
- trade_order_item增加spu_id/spu_name/properties/pic_url，product_id改为sku_id；BaseMapper通过@TableField映射既有productId别名，JSON只在基础设施Converter编码。无重复SKU列、无新表。
- 售后额度读取原订单项，申请时将SKU/SPU/规格/图片固化到trade_after_sale；状态流转不修改原商品快照。XML resultMap覆盖新增字段。
- 报价摘要纳入商品元数据，规格排序及长度编码避免映射顺序和分隔符歧义；同订单号换元数据重放被拒。报价缓存v3使旧预览重新报价，提交恢复v2保留。
- 各接口保持独立DTO/Res，后台订单与售后详情显示下单商品/规格/图片，解码区分历史未知null和已知空规格{}，长ID按字符串保存。
- 初始化DDL与独立升级脚本同步；存量售后SKU只能从原订单项回填，其余未知字段不查当前商品伪造历史。未执行实际数据库升级。

接口合同及示例见 [order-product-snapshot-api.md](../../docs/order-product-snapshot-api.md)。

## 验证证据

- member/mall/trade及依赖已执行clean verify，最终修改后再次verify通过。265项后端测试全部通过，无失败/错误/跳过。日志 `/tmp/snapshot-final-verify.log`、`/tmp/snapshot-verified.log`。
- 真实H2 MySQL模式、生产初始化DDL、MyBatis-Plus Gateway及本地事务：下单后修改SPU名/规格/图片，原订单和售后仍保存旧信息；审批后快照保持不变；报价后改规格拒绝且订单表无写入；同订单号替换图片拒绝。
- 现有折扣+满减+优惠券计价测试增加快照断言；带积分权益及运费的结算缓存/提交集成用例增加原SPU/规格断言。生产Redis序列化器往返保存商品快照；摘要测试覆盖映射顺序、图片变化、null与{}、分隔符不同。
- React 167项测试通过；类型检查及生产构建、ESLint、Prettier全部通过。日志 `/tmp/snapshot-ui-tests-final.log`、`/tmp/snapshot-ui-build.log`、`/tmp/snapshot-ui-lint.log`、`/tmp/snapshot-ui-format.log`。
- 小程序81项测试通过，未修改其页面，新增输出字段兼容现有传输调用。日志 `/tmp/snapshot-miniapp-tests.log`。
- git diff --check通过。本代理未提交、推送或部署；执行期间观察到工作区HEAD被外部更新，保留该更新并以最终工作区运行验证。

## 尚未迁移

properties仍是名称/值Map，缺源Property的propertyId/valueId及商品字典关系，不能标记完全对齐。订单其余金额拆分、源状态字段及售后申请资料仍待逐项迁移。当前46张初始化表，36张业务缺表数量不变，支付演示表仍排除；真实支付/退款/物流渠道SDK继续后置。完整App售后页面、真实Redis/MySQL/Dubbo/MQ、部署HTTP与浏览器/真机端到端未验收。

后续优先迁移商品规格字典关联与结构化快照，再继续订单/售后字段、缺表及前后端业务流程；完整清单仍以27设计和来源manifest为准。
