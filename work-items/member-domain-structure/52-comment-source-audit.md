# 商品评价源代码差异复核

2026-10-29，承接完整迁移F03；本批为源码审计与清单纠正，没有新增业务实现。文章排除。

|能力|源证据|当前状态|
|---|---|---|
|后台分页/详情/显隐/回复|ProductCommentController四个对应方法|已接入，51记录筛选差异|
|后台自评|ProductCommentController#createComment、ProductCommentConvert后台重载、后台CommentForm.vue|未迁移。源明确userId=0且不关联订单；当前order_no非空，需要先调整无购买关系模型，不能伪造订单号绕过|
|会员真实订单评价|TradeOrderUpdateServiceImpl#createOrderItemCommentByMember|已接入真实行ID及状态联动，49记录失败恢复差异|
|系统评价|TradeOrderUpdateServiceImpl#createOrderItemCommentBySystemBySystem|未迁移。源未评行逐项5分、非匿名、空正文，日志34；不能直接复用当前要求非空正文/会员身份的入口|
|公开分页|AppProductCommentController#getCommentPage|已接入，固定可见与评分分组|
|多商品逐项提交|uniapp pages/goods/comment/add.vue|当前逐行跳转独立提交，尚未复刻源同页多商品逐项提交|
|图片选择上传|同上s-uploader|当前HTTPS地址输入，未接上传|
|评价统计/导出|已核对评价Controller/Service/Mapper及后台index.vue|这份参考没有独立接口或操作，不列为源迁移缺口|

纠正之前47至51等历史记录中“评价统计/导出仍待迁移”的判断：本复核替代该判断，历史交付保留不改写。不能因参考缺少这些功能而声称其已实现，也不继续添加。商品/交易整体统计F11是另一业务范围，仍保留。

下一批顺序：先补后台自评的无购买关联模型及独立入口/页面，再处理系统评价与定时触发条件，最后补多商品提交和上传。系统评价不能用会员令牌冒充系统，也不能将真实空正文改成捏造的用户文字。真实订单评价的原内容重试语义继续保留。

验证层次：本批仅本地源码核对和哈希；未跑新增测试、未执行数据库、未commit/push。前批331后端/180前端通过是历史证据，不当作本批新验证。
