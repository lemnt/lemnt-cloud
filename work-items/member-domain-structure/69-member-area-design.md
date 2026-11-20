# 会员地区选择设计

2026-10-29，承接68。来源本地芋道biz-ip/resources/area.csv及AreaController树接口。迁移原CSV为system只读资源，保留全部数据，返回中国(ID1)下省市区树。六层分域area，domain模型/Gateway、infrastructure加载缓存、application DTO/执行器、web独立Res；不新增表/依赖。

GET /areas/tree要求登录，复用现有后台身份，无App公开路由。前端独立API严格解码正int/非空名称与子树，会员表单使用树选择，详情名称沿树路径派生，不把编号当名称。加载/失败明确提示，可重试；没有合法地区树时禁用地区修改，允许其他资料编辑。旧编号无匹配显示未识别并保留，不猜值。member API仍返回areaId，areaName的服务端RPC派生不在本批，注册登录审计另行迁移。

验证源资源哈希、真实资源树省市区路径与不可变、HTTP映射、前端解码和路径/未知值及全回归。未发布路由或浏览器验收，不commit/push。
