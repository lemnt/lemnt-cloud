# 促销后台管理页面

满减、折扣各自页面及菜单component：mall/promotion/reward-activity/index、mall/promotion/discount-activity/index；复用/mall/promotion/reward-activities与discount-activities，没有增加业务接口或数据表。

支持名称/开启状态分页筛选、详情、新增、编辑、关闭及关闭后删除。状态为0开启、1关闭；关闭不可重开，不对已关闭活动提供编辑按钮。list/query/create/update/delete分别使用现有mall:promotion权限，关闭复用update。列表读取失败可重试，详情读取失败不打开编辑表单；服务成功后刷新失败明确提示，false响应不报成功。

满减支持满金额、满整数件数和递增阶梯、全部/SPU/分类范围；从真实商品/分类选择，切换范围清空旧选项。门槛和元金额用字符串/BigInt校验，避免大金额浮点误差。折扣按SPU选真实SKU，改商品清空旧SKU，SKU重复不允许；减价与几折输入分开显示，几折精确转后端基点（8折=8000），隐藏优惠字段不提交。

商品列表/详情、分类选项沿用相应catalog查询权限；没有权限不请求对应接口。请求不携带状态/操作者/其他业务的残留字段。只开放现有后端支持的规则；源满减送的赠券、赠积分、包邮等剩余能力并未实现，本页不提供这些未接通控件。

菜单脚本lemnt-server/sql/V20261031__mall_promotion_menu.sql为新增种子，可重复执行，保留已有商城名称与角色绑定，不分配角色。部署需在system库核对执行并按既有流程授予页面/操作权限；本次未执行真实库或更改角色。

验证：前端最终237项测试及build/typecheck/lint/format通过；后端活动规则与菜单两组10项测试通过。组件验证为SSR及API合同测试，非浏览器交互验收。无真实Nacos、网关/下游集群或数据库操作。
