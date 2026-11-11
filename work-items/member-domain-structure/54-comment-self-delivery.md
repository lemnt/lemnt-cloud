# 后台自评交付

2026-10-29，承接53设计及52源复核。独立POST /mall/comments、Req/Cmd/执行器/Res及mall:comment:create权限已接入，后台表单包含源昵称/头像/SKU/两项评分/正文/图片地址。按源memberId=0标识自评，订单号/订单ID/行ID为空，不调用会员或交易，不改变订单状态；从真实有效SKU/SPU保存商品快照。

旧库仅生成order_no可空升级脚本及独立按钮权限脚本，不授予角色，未执行真实库。前端支持NULL订单号及0会员自评标识；结果未知阻止直接重发，提示先核对列表。每次明确创建为新记录，不承诺内容去重。

相关Maven verify通过：mall118、trade155、member27、gateway32，共332项；React182项、类型/代码/格式检查及构建通过。新增存储用例验证无会员RPC/订单关联、评分、快照、两次创建为不同记录、非法SKU及HTTP图片拒绝；路由测试确认新增权限。未验证部署HTTP、真实权限切面、MySQL升级和浏览器；小程序未改动未重跑。没有新增表，未commit/push。

剩余评价范围：系统评价及触发任务、同页多商品提交、图片上传。文章继续排除；整体初始化47表、剩余32张参考业务表未对应不变。证据见evidence/comment-self-verification.json，接口见docs/product-comment-api.md。
