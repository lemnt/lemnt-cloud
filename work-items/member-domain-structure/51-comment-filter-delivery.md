# 后台评价筛选交付

2026-10-29，code-software-engineer，承接50设计及完整迁移F03。

后台GET /mall/comments新增昵称、真实订单ID、商品快照名称、创建时间起止筛选。各层独立Req/Qry/Filter转换，MyBatis Plus Lambda组合查询；回复状态改按reply_status匹配。昵称和商品名不查询当前资料；历史快照为空时不补造。时间包含边界、可单边输入，倒置返回统一400，非法日期/负订单ID/超长名称返回422。保留原分页与权限。

后台评价页新增对应搜索项和时间范围控件，订单ID保持字符串，false不会丢失。无表结构或依赖变更。文章继续排除。

相关Maven verify通过：mall117、trade155、member27、gateway32，共331项，失败/错误/跳过均0。新增两项后端用例覆盖组合筛选、快照、false回复状态、逻辑删除、时间边界和HTTP绑定/校验。React180项测试通过，类型/代码/格式检查和构建通过。小程序未改动，本批未重跑。

MockMvc为standalone绑定与业务验证，不等价于部署网关权限验收。真实数据库、部署HTTP和浏览器未验证；未commit/push、未执行真实DDL。保留此前工作区变更。评价虚拟创建、自动评价、统计导出和拍照上传仍待迁移；整体47张初始化表、范围内32张业务表未对应的状态不变。

证据：[comment-filter-verification.json](evidence/comment-filter-verification.json)。接口：[商品评价](../../docs/product-comment-api.md)。
