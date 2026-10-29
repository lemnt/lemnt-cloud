# 商品管理接口补齐

> 2026-10-28 配送贯通批次：商品配送字段、地址必填结算、订单运费快照与发货冻结地址见 [配送结算接口变更](delivery-checkout-api.md)。本批已将运费接入下单金额；原文中“尚未接入”的说明仅代表上一批次状态。

> 2026-10-28 业务收敛变更见 [当前接口变更](yudao-alignment-api.md)。本文中的旧兼容入口、独立 SKU 写接口及发布版本机制已退役。

2026-10-29补充：评价的当前字段、页面及公开接口以 [商品评价接口](product-comment-api.md) 为准；下文保留初始交付批次范围。

参考芋道商品管理的分类、品牌、SPU、SKU、规格字典和评价管理能力，按当前项目的分层、权限和库存边界实现。此文档记录后端接口交付；新增管理页面尚未交付。

## 命名与边界

| 业务对象 | Java 命名 | 持久化 |
| --- | --- | --- |
| 通用商品 | `Product` | 保留现有通用商品契约 |
| 商品款式 | `ProductSpu` | 现有 `mall_product_spu` |
| 可销售规格组合 | `ProductSku` | 现有 `mall_product_sku` |
| 商品规格 | `ProductSpec` | 新增 `mall_product_spec` |
| 商品规格值 | `ProductSpecValue` | 新增 `mall_product_spec_value` |
| SKU 规格快照 | `ProductSkuSpecDO` | 现有 `mall_product_sku_spec` |
| 商品评价 | `ProductComment` | 新增 `mall_product_comment` |

颜色、尺码等参与 SKU 组合的内容属于规格；材质等描述信息属于属性，后续业务落地时使用 `ProductAttribute` / `ProductAttributeValue`，本次不增加占位模型或表。SKU 的 `ProductSkuGateway`、`ProductSkuMapper`、`ProductSkuDO` 统一对应 `mall_product_sku`，删除原重复 SKU 数据投影。既有商品接口地址、SKU `properties` 字段及规格快照表保留兼容。

Adapter 按 app/web 后再分域；请求和响应按接口独立定义，分页统一使用 `R.page`。分类、品牌、规格、评价与库存统一归入 product 域，分别维护各自的应用逻辑；数据库访问遵循按表 Gateway、MyBatis Plus Mapper 和 DO。

## 管理接口

网关前缀沿用现有配置，下表为服务内路径。权限使用统一 `@RequiresPermissions`。

| 方法 | 路径 | 用途 / 权限 |
| --- | --- | --- |
| GET | `/mall/categories/tree` | 分类树，`mall:category:list` |
| GET | `/mall/brands/page` | 品牌分页，`mall:brand:list` |
| GET | `/mall/brands/simple` | 启用品牌选择，`mall:brand:query` |
| POST | `/mall/spus/{id}/recycle` | 移入回收站，`mall:spu:edit` |
| POST | `/mall/spus/{id}/restore` | 从回收站恢复，`mall:spu:edit` |
| DELETE | `/mall/spus/{id}` | 删除回收站商品，`mall:spu:remove` |
| GET | `/mall/spus/count` | 按名称、分类和品牌统计状态，`mall:spu:list` |
| GET | `/mall/spus/simple` | 有限条上架商品选择，`mall:spu:query` |
| GET | `/mall/spus/batch` | 批量详情，`mall:spu:query` |
| POST | `/mall/spus/{spuId}/skus` | 添加 SKU，`mall:sku:add` |
| GET | `/mall/skus` | SKU 分页，`mall:sku:list` |
| GET | `/mall/skus/{id}` | SKU 详情，`mall:sku:query` |
| PUT | `/mall/skus/{id}/shelf` | SKU 上下架，`mall:sku:edit` |
| DELETE | `/mall/skus/{id}` | 删除 SKU，`mall:sku:remove` |
| GET | `/mall/skus/{id}/stock-records` | 库存调整流水，`mall:stock:list` |
| GET / POST | `/mall/specs` | 规格分页 / 新增，`mall:spec:list/add` |
| GET | `/mall/specs/simple` | 规格选择，`mall:spec:query` |
| GET / PUT / DELETE | `/mall/specs/{id}` | 规格详情 / 编辑 / 删除，`mall:spec:query/edit/remove` |
| GET / POST | `/mall/spec-values` | 规格值分页 / 新增，`mall:spec-value:list/add` |
| GET | `/mall/spec-values/simple` | 按 `specId` 选择规格值，`mall:spec-value:query` |
| GET / PUT / DELETE | `/mall/spec-values/{id}` | 规格值详情 / 编辑 / 删除，`mall:spec-value:query/edit/remove` |
| GET | `/mall/comments` | 评价分页，`mall:comment:list` |
| GET | `/mall/comments/{id}` | 评价详情，`mall:comment:query` |
| PUT | `/mall/comments/{id}/reply` | 商家回复，`mall:comment:reply` |
| PUT | `/mall/comments/{id}/visible` | 评价显示状态，`mall:comment:edit` |

已有分类、品牌和 SPU 的增改查、上下架以及库存调整入口继续使用；SPU 分页新增 `categoryId`、`brandId` 筛选。品牌分页不依赖原列表的条数上限。所有选择查询有条数上限，批量详情最多 100 个编号，保持请求顺序并去重。分页请求和选择请求的约束以各接口独立 Req 为准。

规格新增示例：`{"name":"颜色","remark":"销售规格"}`；规格值新增示例：`{"specId":1,"name":"黑色","remark":null}`。规格值的所属规格创建后不可更换。字典用于选择，不强制改写既有 SKU 字符串快照。已引用的规格名称和值禁止修改或删除；允许修改或清空说明。名称唯一，规格值在所属规格内唯一。

## 状态和库存约束

SPU 状态为 `ON_SHELF`、`OFF_SHELF`、`RECYCLED`、`DELETED`。只有下架商品可进入回收站，恢复后仍下架，只有回收站商品可删除。删除为终态逻辑删除，保留订单、评价和库存流水的历史编号，普通查询不返回已删除数据；普通编辑和上下架不能绕过恢复操作。

SKU 状态独立于 SPU。添加和删除 SKU 要求 SPU 下架；同一 SPU 最多 100 个 SKU，规格维度必须一致、组合不可重复、商品编码不可重复，不能删除最后一个 SKU。存在预占库存的 SKU 不能删除；SPU 删除同样检查全部子 SKU，并在同一事务中完成。删除时释放规格组合键，重新添加同组合需要新的 SKU 编码和编号。上架 SPU 至少需要一个上架 SKU。

库存通过既有库存接口修改，不通过规格或 SPU 资料编辑改库存；已删除 SKU 不允许新增库存调整，历史流水仍可查询。下单预占、支付后的确认、回滚仍使用现有链路。

## 评价来源与管理

`POST /mall/app/comments` 为真实购买评价入口，会员身份从网关安全上下文获取，不接收客户端指定会员。请求为 `orderNo`、`skuId`、`score`（1—5）、`content`（最多 1000 字）。通过既有远程订单接口校验订单属于本人、已完成且包含该 SKU；一个订单的一个 SKU 只能评价一次。同内容重试返回原记录，并发重复写入也返回同一记录；不同身份、评分或内容不能覆盖已有评价。已删除商品的真实历史购买仍可评价。

后台可以按商品、会员、评分、显示状态和是否回复筛选评价，修改显示状态和商家回复。回复身份取后台安全上下文，相同内容重试保留回复时间。没有管理端伪造购买评价入口。初始交付批次未提供图片、公开评价列表及页面；2026-10-29已补充两项评分、图片/匿名、真实会员及订单商品快照、公开分页、后台与小程序页面，见当前接口文档。评价统计、导出及源虚拟评价创建仍未迁移；不伪造头像或昵称。

## SQL 与验收边界

- 新库按现有脚本顺序执行，`V2__mall_catalog.sql` 已包含扩展 SPU 状态；新增 `V16__product_management.sql` 创建上述三个业务表，并从既有 SKU 快照提取规格选择字典。
- 已有库先执行 `lemnt-mall/sql/upgrade/product_management.sql` 更新状态约束，再执行 V16。升级脚本是一次性迁移，不应反复执行。
- 系统库 `V20261027__product_management_permissions.sql` 仅向既有商城根目录添加缺失功能权限，可重复执行；不创建页面、不自动授予角色。
- 本地验证使用 H2 MySQL 模式和 HTTP 测试入口，未执行真实 MySQL 升级、部署服务或完成浏览器端新增页面验收。

参考：[芋道商品模块](https://gitee.com/zhijiantianya/yudao-cloud)。参考源码校验记录见工作项证据 `product-reference-source.json`；当前实现保留本项目接口和事务边界，并不声明与参考项目所有功能完全一致。
