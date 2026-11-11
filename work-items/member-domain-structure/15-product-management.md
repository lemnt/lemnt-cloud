# 商品管理补齐与命名统一

## 目标

参考芋道补齐已有商品后台接口，包含商品评价管理；沿用 system/member 的层次、统一分页和异常、每接口独立 Req/Res、按表 MyBatis Plus Gateway/Mapper/DO。按用户约定使用 Product、ProductSpu、ProductSku、ProductSpec、ProductSpecValue；描述性属性后续使用 ProductAttribute、ProductAttributeValue，不增加占位模型。

## 实现

- 分类树、品牌分页和选择、SPU 回收站/恢复/删除/统计/批量详情及筛选。
- SKU 新增、独立分页详情、上下架和删除，保留真实库存预占限制与历史记录。
- 规格及规格值字典，已有引用保护、唯一约束、说明清空、历史快照回填。
- 真实完成订单产生评价，后台分页详情、商家回复和显示管理，重复请求及并发幂等。
- SKU 统一实际表数据对象，清除重复投影，保持既有接口字段及历史表名兼容。
- 新增规格、规格值、评价三张表迁移脚本和缺失功能权限脚本；不自动授权、不伪造页面。
- 前端兼容 SPU 回收站状态，并保护已删除状态和 SKU 状态边界；新增管理页面未实现。

## 验证

具体结果见 `evidence/product-management-verification.json`。覆盖持久化、回收站状态、库存删除阻断和事务回滚、规格引用约束、历史数据回填、大量品牌分页、批量详情顺序、真实订单评价权限及并发、HTTP 入参隔离和网关身份、路由与权限集合、权限 SQL 重复执行及角色不变。

接口和 SQL 使用说明：[商品管理接口](../../docs/product-management-api.md)。真实 MySQL 迁移、部署及浏览器联调未执行。没有提交或推送代码。
