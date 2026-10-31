# 配送贯穿结算与订单设计

2026-10-28 v1，code-software-engineer。承接23/24配送基础批次，参考本地芋道 TradeDeliveryPriceCalculator、MemberAddressApi、ProductSpu/SkuDO、TradeOrder/ItemDO 和售后计算器。沿用既有Java/CQRS规范和已读取技能参考。

范围：普通快递（delivery_type=1）；不提前实现自提。SPU保存delivery_types/delivery_template_id，SKU保存weight/volume；管理入参和详情同步。新增会员地址专用RPC，只按memberId+addressId读取本人地址；Mall经Gateway调用，HTTP不能指定收件人、地区、运费或价格。

结算预览请求新增必填addressId，pointStatus必须完整传至应用层（现有Req→Cmd丢失该字段一并修复）。服务端读取购物车、商品、会员券及地址，调用trade运费RPC，保存收件快照和逐行运费。下单重读会员地址/商品/配置报价并比对缓存摘要，变化则要求重新确认；原请求已创建订单时返回原记录，之后修改地址不得改变订单快照。

金额：现有数据库/接口为精确元，trade计价契约为整数分，Gateway转换；商品行payAmount包括分摊运费，deliveryPrice独立记录。原价-商品优惠+运费=实付，行实付合计=订单实付，行运费合计=订单运费。旧历史订单配送快照允许NULL；新公开结算必须选择本人地址，不能用默认零运费。

订单落库根记录配送方式、收件姓名/手机/区划/详细地址、运费；明细落库运费。Trade RPC验证金额守恒、运费分摊和收件快照完整，重复订单比较配送内容。历史构造器仅用于历史读取/原有内部用例，不能绕过新HTTP必填校验。

售后使用不可变行实付及运费快照校验上限，保持当前按数量售后及幂等，不用当前运费模板重算旧订单。运费是否允许退还按参考售后服务核对后实现并记录，不凭猜测。

任务：C1 商品字段及上下层传递→生产Mapper保存往返；C2 地址/运费RPC及Gateway→归属/单位/规则测试；C3 结算缓存/摘要/订单RPC/DO完整传递→带运费下单与重复提交；C4 miniapp选择本人地址和展示运费、管理编辑商品配送字段→前端测试；C5 缓存变化/跨会员/售后金额→集成回归和文档。新增MallV17/TradeV8，不执行真实数据库，不提交/推送。未配置配送的旧商品应由管理员补齐后才能在新公开结算购买，不暗中映射默认模板。
