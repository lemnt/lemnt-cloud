# 移除加购请求表与结算单命名

- 删除 CartAddRequestGateway、实现、Mapper、DO、XML 及 CartAddition 请求记录模型。
- 保留现有加购接口地址，quantity 改为购物车最终数量，移除 requestNo、addedQuantity 字段；会员锁、商品可用性及库存上限校验保留。
- 小程序同步最终数量提交与回执核验，失败重试复用同一目标数量；页面说明覆盖现有数量。
- 新库不创建 mall_cart_add_request，跨库迁移不复制；已有库整理脚本归档旧表，不直接删除真实数据。
- OrderSnapshot 重命名为 SettlementOrder（结算单），同步 Gateway、应用服务和测试引用，Redis 字段结构不变。

Mall/Trade 相关模块 clean test 通过，共 293 个测试，失败/错误/跳过均为 0；小程序 70 个测试通过。未提交、推送或修改真实数据库。
