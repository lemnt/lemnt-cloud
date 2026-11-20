# 后台评价筛选设计

2026-10-29，code-software-engineer，承接27完整迁移F03。参考本地芋道ProductCommentPageReqVO及ProductCommentMapper：昵称/商品快照名称模糊查询、真实订单ID、评分、回复状态、创建时间范围。沿用已有SPU/会员/显隐条件与统一分页，不新增表。文章继续排除。

现有GET /mall/comments增加userNickname、orderId、spuName、createTimeStart、createTimeEnd。时间使用ISO本地时间（不带时区），两端可独立提供，包含边界；倒置范围返回统一400，非法格式由统一绑定校验处理。名称最长255，空白按无条件；订单ID为正数。单独Req/Qry/Filter逐层转换，普通筛选用MyBatis Plus Lambda；回复按已有reply_status而非正文是否为空。现有reply_status为非空列，沿用既有升级脚本从真实回复内容初始化的状态，不改表结构。

后台现有表格开启昵称/商品名搜索，增加订单ID与时间范围。查询保留长ID字符串及false状态；不添加统计/导出或虚拟评价。测试验证组合过滤、时间边界/倒置、逻辑删除、false回复状态，前端验证请求参数，执行相关后端与前端回归。真实数据库与浏览器未验收不得标PASS。
