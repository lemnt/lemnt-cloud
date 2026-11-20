# 商品售罄、警戒库存及Excel导出迁移

2026-10-31第133批，读取本地芋道ProductSpuMapper.appendTabQuery及ProductConstants，并按当前SKU库存归属补齐两个库存分组和同步Excel下载。没有新增业务表、冗余SPU库存字段或修改网关引用。

库存分组与上下架状态分开：分页新增可选stockStatus=SOLD_OUT/LOW_STOCK，status继续表示真实上下架状态。SOLD_OUT为全部未删除SKU可用库存之和等于0，沿用源逻辑，包含未删除的回收站商品；LOW_STOCK为总量不超过10且排除回收站，包含库存为0的商品。DELETED商品和SKU不参加。两个库存分组与上下架分组重叠，不能把五个计数相加当商品总数。

普通商品CRUD/分页继续使用MyBatis Plus；SKU聚合条件需要关联子查询，放ProductSpuMapper.xml并使用已有ProductSpuResult。库存分组的分页与数量使用同一XML条件片段，名称、精确分类、品牌条件一致。没有在Mapper重复写普通CRUD，也没有手拼用户SQL。新增ProductStockStatus属于product域。预占减少可用库存，回滚恢复，查询读取当时真实SKU存量，不把预占量再补回统计。

前端五个分组均由服务端真实数量驱动，未读取时显示未知。库存分组通过stockStatus请求，响应行的status仍保留实际商品状态；上架、回收、恢复、删除按钮依旧按行真实状态与权限控制。

新增GET /mall/spus/export，独立ProductSpuExportReq/Qry/Res与mall:spu:export权限；按当前名称、分类、品牌、状态/库存分组分页读取完整匹配结果，不接受客户端页码。单次最多10000条，超限提示缩小范围，读取不完整时拒绝生成截断文件。在同一只读事务中读取，不引入异步导出任务、MQ或OSS运行配置。

导出使用项目已管理版本的EasyExcel直接依赖。未引入common-excel模块，因为其自动配置还会启用任务与OSS；本次仅需同步文件输出。文件包含当前真实的商品编号、编码、名称、分类编号、品牌编号、状态与描述。编号为文本，大Long不丢精度；名称等文本不作为公式。尚未迁移的经营/审计字段不填默认值、不混入文件。网页使用现有认证HTTP客户端接收Blob，JSON/ProblemDetail/401和空文件均拒绝下载；独立导出权限，不用列表或编辑权限替代。

新增system库V20261031__mall_spu_export_permission.sql，复用已有商品菜单，只登记按钮，不分配角色。H2验证无商品菜单时不生成孤立按钮、重复执行不新增、原授权保留；真实库未执行。

验证：265项前端测试/46文件；38项后端聚焦测试（导出分页2、商品HTTP7、实际XLSX HTTP1、商品持久化27、菜单1）通过。包含0/10/11边界、多SKU合计而非单SKU、回收/删除排除边界、预占与回滚、过滤一致性、导出完整分页和超限拒绝，以及POI打开实际生成的XLSX校验大编号、文本和状态。MockMvc Excel测试使用-Djava.awt.headless=true，避免独立JUnit进程初始化桌面字体环境。前端API/SSR，后端MockMvc/H2均不是浏览器或真实部署验收。

build（含tsc）、lint、format:check、git diff --check通过，只有既有vendor大chunk提示。证据evidence/spu-stock-export-verification.json；日志/tmp/spu-stock-ui-*.log、/tmp/spu-stock-backend.log。未运行全后端/小程序回归，未真实数据库/Nacos/Redis/Dubbo/浏览器联调，未commit/push。

源SPU完整经营字段、列表库存汇总展示和创建时间筛选/历史审计、导出全部源经营列等仍待迁移。源SPU页和API映射保持in_progress，不将本批当整页完全等价完成。
