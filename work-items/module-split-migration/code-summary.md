# 四模块拆分代码进度

整体生成进行中，未完成全部迁移。

## P1 product

已创建六层商品服务、独立10张业务表与Outbox/Inbox；迁移商品/规格/评价/库存及必要会员ACL。评价通过独立订单端口，过渡期只消费旧订单Client。库存预占与事件同事务，行锁XML显式禁缓存。

已通过93项模块测试及package，详情见evidence/p1-product-tests.json。未执行真实数据库/Nacos/MQ/Dubbo/HTTP验收，旧mall/trade仍保留。P2～P5待执行。

## P2 pay

已迁移支付/退款/钱包/充值与配置HTTP，11业务表+Outbox/Inbox独立建表。回调与退款只更新资金和事件，原订单/售后经可信Client核对。金额为0不创建支付退款流水。原通知任务仅登记资金事件，历史AFTER_SALE任务须在真实切流前审核归属。

已通过107项测试，package通过。支付库没有订单/售后表的新测试验证了金额守恒、重复通知、关单后真实付款保留、冻结退款和零现金行为。拆出的售后退款HTTP断言已保存至order测试，待P4完成验证。为P2依赖提前建立order-client，完整order实现仍在P4。
