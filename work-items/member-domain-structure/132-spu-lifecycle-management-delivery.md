# 商品回收站及状态管理页面迁移

2026-10-31第132批，对照芋道商品SPU管理页，接通已有业务服务的回收、恢复、删除、状态数量与分类品牌筛选。没有新增业务表、后端业务逻辑、依赖、渠道适配或网关RPC引用。

商品页分出售中、仓库中、回收站三个分组，初始出售中。数量调用服务端/mall/spus/count，不由当前页行数计算，未读取时显示未知。列表与计数使用同一名称、精确分类ID、品牌ID；计数不发送当前状态、分页字段。筛选、切换分组回到第一页。分类品牌来自有权限的真实字典，读取失败可重试，未知名称显示实际ID，不猜名称。

出售中仅可下架，仓库中可上架或回收，回收站可恢复或删除。恢复后为下架，不能直接上架；仍有库存预占时由已有服务拒绝删除。删除保留历史订单、SKU与台账，UI明确不可恢复。接口删除、回收、恢复必须返回true才报成功。状态操作成功刷新列表、数量和详情缓存；若当前最后一页只剩该记录则退回上一页。编辑前重新读详情，已回收时禁止打开编辑。详情抽屉补回收状态名称。

复用/mall/spus/{id}/recycle、/{id}/restore、DELETE /{id}、/count。页面/查询/编辑/删除继续使用现有mall:spu:list/query/edit/remove权限，不把编辑权限当作删除权限。菜单仍为mall/spu/index；没有新增菜单脚本，沿用V20261025__mall_catalog_menu.sql和V20261027__product_management_permissions.sql，未自动授权或执行真实库。

验证：260项前端测试/45文件，32项后端聚焦测试（ProductManagementHttpTest 6、CatalogPersistenceTest 26）通过。前端新增5项覆盖同筛选列表/数量合同、回收恢复删除路径和失败处理、SSR按行状态/权限显示操作、未取得数量不填0。SSR使用缓存中的行测试操作条件，不是浏览器交互验收。生产Jackson MockMvc核对统计参数绑定及生命周期Boolean响应；H2真实Mapper原有场景覆盖下架才能回收、恢复仍下架、删除不可复活、预占阻止删除及事务回滚。

build（含tsc）、lint、format:check、git diff --check通过，仅既有vendor大chunk提示。证据见evidence/spu-lifecycle-management-verification.json；日志/tmp/spu-lifecycle-ui-*.log、/tmp/spu-lifecycle-backend.log。未重复全后端或小程序回归，未做真实环境/浏览器/Nacos/数据库联调，未commit/push。

源已售罄、警戒库存分组的完整模型和计数、创建时间筛选、完整经营字段及导出仍未在本批实现，没有伪造分组数字或把源SPU整页标记完成。源映射保持in_progress。
