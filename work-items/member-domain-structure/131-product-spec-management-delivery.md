# 商品规格、规格值管理迁移

2026-10-31第131批，参考芋道本地product/property和property/value管理页面及Mapper，沿用ProductSpec/ProductSpecValue命名、本项目分层、独立HTTP Req/Res、MyBatis Plus与现有表。

新增规格分页、名称及创建时间范围筛选、详情、名称备注增改删除；规格值由父规格进入，固定归属，按名称分页并维护。规格名称64字、值名称100字、备注500字且可清空。独立列表/详情/增改删权限，详情成功读取后才编辑，错误不打开空表单；删除false不提示成功。操作后刷新列表和商品缓存。商品编辑页支持手工刷新规格字典，不清空选择。

补齐现有create_time从DO到领域、DTO、独立分页/详情响应的转换；规格项支持ISO本地时间起止查询，包含两端、单侧过滤、拒绝倒置。规格值仅展示创建时间，保持源页面的所属规格/名称筛选，没有添加额外时间筛选。两种分页按源Mapper改为编号倒序。创建时间只从持久化读取，改名不重写。Lambda实现简单查询，没有新增Mapper CRUD方法、XML查询或业务表。新增/修改请求均不接受创建时间。

两个菜单组件注册，规格值页隐藏于侧栏并由规格行进入。系统库V20261031__mall_spec_menu.sql可重复执行，保留旧商城名称及角色绑定，不自动授予角色。仅在H2演练，真实库未执行。源前端对应页面/API标记in_progress，包含代码合同验证但浏览器和真实环境联调待验。

契约说明见docs/product-spec-management-pages.md；验证证据见evidence/product-spec-management-verification.json。未执行部署、真实数据库/Nacos/网关/Dubbo验收，未commit/push。创建时间、倒序、时间边界、字典改名及删除限制均有真实Mapper/H2或MockMvc证据。

最终255项前端测试/44文件、31项后端聚焦测试通过；build（含tsc）、lint、format:check、git diff --check通过，仅既有vendor大chunk提示。日志/tmp/spec-ui-*.log及/tmp/spec-management-backend.log。
