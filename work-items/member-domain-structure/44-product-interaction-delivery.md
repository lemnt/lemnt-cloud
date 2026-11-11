# 商品收藏与浏览足迹交付

2026-10-29，承接27完整迁移F03、43设计。本批参考芋道 product_favorite、product_browse_history 的 DO、服务、控制器及 uni-app 收藏/足迹页面，迁移业务逻辑、存储、调用页面和权限，不宣称全项目迁移完成。

## 已实现

- 新增 mall_product_favorite、mall_product_browse_history，独立 DO/BaseMapper/Gateway、应用 Cmd/Qry/DTO/Executor、App/Web Controller 和逐接口 Req/Res，分页仍用 R.page。简单存储用 Lambda，无重复 CRUD/XML。
- 收藏重复报409，取消不存在报404；足迹按会员+商品替换最近记录、保留最多100条有效记录，会员移除/清空只隐藏，重访恢复。沿用源逻辑删除，后台可以筛选 userDeleted=false/true。写入复用基础会员锁，释放跟随事务结束，裁剪和插入共同回滚。
- App只取当前会员，无法通过 Body/Query 指定他人；公开商品详情仍由现有网关处理，登录后独立受保护记录足迹。mall不增加Token认证或会话服务。
- React商品详情内按权限查询收藏、足迹，保留 false 筛选；权限SQL仅增加两个功能权限，无新增主菜单/自动角色授权。
- 小程序商品详情收藏、状态核验与登录后足迹记录；会员入口及两类分页列表、当前页全选、单条/批量移除、足迹清空。换号丢弃旧响应，收藏批量每项核对身份；部分失败停止、保留错误并重查实际结果。

## 验证

|范围|结果|证据边界|
|---|---|---|
|mall/trade/member及依赖 Maven verify|282项通过，失败/错误/跳过均0|包括6项新持久化及控制器边界测试|
|React测试|172项通过|API解码/筛选等单元回归|
|React生产构建、lint、格式检查|通过|不是浏览器操作验收|
|小程序测试|93项通过|传输、页面逻辑、换号、未知结果、批量和失败提示|
|git diff --check|通过|无提交或推送|

新持久化测试在H2执行生产V17 DDL、使用真实MyBatis Plus Gateway/事务；基础LockTemplate的Redis执行端使用本地锁替身。覆盖并发收藏唯一、足迹上限与重访、隐藏与管理员可见、跨会员隔离、裁剪插入失败回滚、HTTP参数和统一错误边界。没有执行真实MySQL、Redis/Dubbo/MQ、部署网关HTTP、浏览器或设备端到端验收。

当前初始化SQL共47张表，剩余34张参考业务表无对应表，另2张演示表不迁；缺表减少不代表已有表全部字段及业务已完全对齐。逐表剩余列表、内容哈希和验证日志位置见 [本批证据](evidence/product-interaction-verification.json)，源清单只标 in_progress。

## 适配与待验收

接口明细见 [当前接口文档](../../docs/product-interaction-api.md)。源价格分单位适配当前元金额；资料不存在保留null，源销量等尚无完整事实的字段未补造。后台采用React商品详情只读面板，小程序采用原生结构；尚未逐像素复刻源Vue/uni-app样式。商品详情受保护POST记录为现有网关边界下的适配。

V17初始化SQL和两个权限种子仅生成，没有执行真实数据库升级或权限授权。当前仓库历史身份相关表仍在旧初始化SQL中，不能将它们计为本批新增认证功能或已完成清理。
