# 商品关键字、简介、轮播图、排序迁移

2026-10-31第134批，对照芋道InfoForm/OtherForm及保存请求，补齐关键字keyword、简介introduction、轮播图sliderPicUrls、排序sort，从表、DO、领域、应用命令/DTO、独立HTTP Req/Res到后台编辑、详情和导出同步贯通。没有新增业务表或改动支付、库存写入、认证、网关RPC引用。

后台新建/编辑明确要求关键字（最多255字）、简介（最多128字）、非负整数排序和轮播图集合。轮播图每项URL必填且不超过500字、最多5张，沿用源UploadImgs默认数量；当前允许清空集合，按输入顺序存JSON并返回。后台沿用现有商品页URL维护方式并增加图片预览，尚未迁移源上传/拖拽交互。创建和修改HTTP请求分别定义，页面sliderPictures只作为表单结构，提交时转换为sliderPicUrls字符串数组，不混入请求。

普通分页、库存分组、商品选择及商城商品列表均按sort DESC/id DESC排序，导出使用同一商品分页。状态计数口径不受排序影响。导出新增关键字、简介、按顺序换行的轮播图地址及排序四列；仍只导出已真实存储的字段，大编号保留文本。App页面的关键字/简介/轮播展示暂未在本批补齐。

新库V2__mall_catalog.sql加入四列。已有库使用sql/upgrade/spu_operating_fields.sql，执行前确认列未添加，只执行一次，不能重复跑新库初始化。旧关键字、简介、轮播图保持NULL，不拿商品名补造；排序默认0保持原编号倒序行为。实际SQL仅H2验证，真实库未执行。新HTTP写入契约要求这些字段，更新前端与服务需一起发布，已有记录首次编辑时补填必填信息。

领域/应用复制商品及重组SKU、批量详情时保留经营字段；历史内部调用未携带字段时保留原配置，Gateway也仅写入有值的经营字段，显式[]可清空轮播图，0可更新排序。既有公开请求新增校验；历史便利构造仅保留原有内部使用，没有新增兼容入口或占位模型。

验证：266项前端测试/46文件通过，build（含tsc）、lint、format:check通过，仅既有vendor大chunk提示。mall全部31个测试类132项通过、无失败/错误/跳过。覆盖真实Controller/H2落库、元数据修改和内部旧调用不丢失、多SKU重组、普通/库存分页排序、升级保留原数据、字段校验、真实XLSX新增列。同步修正上一批新增导出路由的ControllerMappingTest合同记录。范围为mall全量测试，不代表全项目或真实部署验收。

证据evidence/spu-operating-fields-verification.json，日志/tmp/spu-operating-ui-*.log、/tmp/spu-operating-backend.log。未执行真实数据库、Nacos、Redis/Dubbo/浏览器/小程序验收，未commit/push。

源创建时间/审计与时间筛选、SPU库存合计展示、更多SKU经营字段、赠积分/虚拟销量等仍待迁移。源InfoForm/OtherForm映射只标in_progress；OtherForm本批仅完成排序，不把赠积分和虚拟销量当成已迁移。

跨模块补验：同步适配trade下单集成测试中两处完整SPU构造；CheckoutFlowIntegrationTest全部100项通过，无失败/错误/跳过，日志/tmp/spu-operating-trade-integration.log。未修改trade生产业务逻辑；这100项为下单链集成回归，不代表trade全量测试。
