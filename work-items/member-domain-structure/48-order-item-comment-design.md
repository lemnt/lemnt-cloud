# 订单项评价联动设计

2026-10-29 v1，责任 code-software-engineer，承接46/47评价链路与完整迁移F02/F03；文章仍排除。沿用已读取的工程技能设计/API/Java/TS规范及项目AGENTS，参考本地芋道TradeOrderUpdateServiceImpl#createOrderItemCommentByMember、ProductCommentDO和订单/订单项commentStatus及MEMBER_COMMENT(33)。

## 目标与边界

trade_order_item已有真实自增id，补到domain→client→mall订单明细→独立HTTP响应，不用SKU生成行ID。新增订单项/订单评价状态及源finish_time：新订单明确未评；旧行状态未知保留NULL，只有真实评价完成确认后更新。报价和Redis结算快照没有订单项ID/评价状态，不纳入计价指纹或创建订单幂等比较，不能用后续状态变化修改原订单金额。创建时拒绝/忽略调用方传入行ID和评价状态，由数据库及业务生成。

写入编排由trade持有订单锁并核验本人COMPLETED订单及所选行，调用mall的私有ProductCommentClient保存真实评价，再同一trade事务更新订单项状态；所有项确认为true才将订单comment_status设true并记录一次33号日志和finish_time。mall私有保存不回查trade，只接收trade已验证的不可变购买事实，会员资料仍走现有member客户端，避免循环RPC和跨库查询。现有/mall/app/comments作为兼容HTTP入口，身份仍取网关，经TradeOrderClient业务写入方法到trade编排；不新增Token逻辑、消息/请求表或outbox。

远程评价已提交而trade事务/响应失败时不能声称全局回滚。原完整请求重试由mall评价唯一约束返回原ID，再修复trade状态；已评价项仍核验完整原内容，不能盲目返回成功或重复日志。trade在本地事务结束前调用远程保存，mall不反向查询订单，避免锁循环。重复完成不覆盖finish_time。源自动评价任务不在本批范围。

## 数据与兼容

mall_product_comment新增order_item_id，改为真实订单项唯一；历史NULL只能用经过trade核验且该SKU在订单中唯一的旧orderNo+SKU记录绑定，不猜测有歧义的旧评价归属，不回填当前商品资料。保留原订单号用于审计与旧客户端定位。并发旧行绑定/新行保存都受订单项唯一约束保护。

新增或调整的多字段写入用Cmd/Bo/领域记录，RPC对象与应用/domain/HTTP各自转换，DO/Mapper/Gateway按表，简单CRUD和条件更新用BaseMapper/Lambda。无新业务表。存量脚本分服务执行，仅生成不运行真实库；不能自动跨库join回填旧评价。MySQL物理升级与真实RPC仍需后续部署验收。

## 接口与页面

- 原会员创建Req增加可选orderItemId（新页面必传），保留orderNo/skuId兼容；同时传入时必须属于同一真实订单行。请求不得设置commentStatus/订单/商品/会员快照。
- 私有TradeOrderClient创建评价Bo与ProductCommentClient可信购买保存Bo为不同用途，不暴露为HTTP请求体。
- 会员订单明细增加orderItemId/commentStatus；订单回执增加commentStatus/finishTime。报价明细对应值为空；不向客户端伪造“未评”旧状态。
- 后台评价显示原订单项编号。小程序从完成订单行读取实际ID，已评行显示已评价；原请求未知结果继续冻结并原样重试，不能因订单行已评就阻断原内容核验。旧URL从服务端订单读取实际ID后再提交，不使用SKU作为行ID。

## 任务与验证

1. 表字段/投影/幂等比较→生产H2存储真实行ID往返，元数据变化不破坏原订单重试，忽略伪造ID/状态。
2. trade编排及mall可信保存→双库真实组件桥接，会员/完成状态/行归属、单项/全部项、重复日志及时间、并发、远程已提交响应丢失/本地回滚后的原请求修复。
3. 历史兼容→NULL保持未知、唯一SKU旧评价绑定、不绑定歧义旧记录、JSON快照不被补当前资料。
4. 前端/文档→实际ID入参与已评展示、未知结果原样重试、换号/伪造行输入、相关Maven clean verify、React测试/构建/规范、小程序回归。

接口文档与交付分别更新，未执行的MySQL/Redis/Dubbo/部署HTTP、浏览器及真机不得标PASS。不得commit/push，不改用户已有暂存状态。
