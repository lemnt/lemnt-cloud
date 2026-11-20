# 商品规格与规格值管理页面

2026-10-31第131批，参考本地芋道 `mall/product/property` 及 `property/value` 页面与对应服务。参与SKU规格组合的字典继续使用用户确认的 `ProductSpec`、`ProductSpecValue`，不额外建立ProductAttribute占位表。沿用当前React、Ant Design、统一表单及已有服务路径。

商品规格在 `/mall/specs` 菜单进入（component=`mall/spec/index`），支持名称、创建时间范围分页筛选、详情、新增编辑删除。点击某规格的“规格值”进入 `/mall/spec-values?specId=...`（component=`mall/spec-value/index`）；该页面隐藏于侧栏，固定所属规格，按名称分页并维护规格值。创建时发送所属规格ID，修改请求只发送名称和备注，不能更换归属。拥有规格项权限不自动获得规格值权限。

两种记录展示真实创建时间。新增规格项GET分页筛选参数 `createTimeStart`、`createTimeEnd`，格式为ISO本地时间，包含两端；范围倒置拒绝，单侧可独立过滤。规格值筛选保持源页面的所属规格与名称，不增加时间筛选。规格和规格值按源Mapper以编号倒序分页。现有BaseDO/create_time已具备字段，本批仅补领域、应用、响应转换和规格项Lambda时间条件；创建时间由持久化填充，修改名称或备注不会重写它。读取缺失时间时显示空，不伪造当前时间。

规格名称最多64字、规格值名称100字，备注500字，可清空。命名唯一和父规格存在性由现有服务及唯一索引校验；规格含子值时不能删除。改名同步当前SKU字典名称，删除值保留SKU/订单快照；既有订单快照不随改名重写。编辑前读取详情，失败不打开空表单；保存新增/修改返回记录，删除必须返回true，业务失败不提示成功。大编号按字符串保留、拒绝丢失精度或Long越界。所属规格缺失/错误时禁止进入值管理。商品编辑页增加“刷新规格字典”，重新请求选择数据，不清除已有选择。

接口保留 `/mall/specs`、`/mall/spec-values` 前缀。后端每个HTTP接口保持独立Req/Res，分页保持R.page的rows/total。页面与按钮分别使用 `mall:spec:list/query/add/edit/remove`、`mall:spec-value:list/query/add/edit/remove`。新增系统库脚本 `lemnt-server/sql/V20261031__mall_spec_menu.sql`，可重入，保留现有商城根名称及角色绑定，不自动授予角色。没有新增业务表、依赖或网关RPC引用。

验证采用前端API合同/SSR、生产Jackson的MockMvc、H2真实Mapper。未做浏览器点击、真实MySQL/Redis/Dubbo/网关联调；SQL仅H2验证，真实库未执行。源页面映射标记in_progress，不能把此批视为全商城迁移或实际联调完成。
