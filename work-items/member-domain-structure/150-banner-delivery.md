# 营销扩展：Banner 迁移交付

2026-11-01 第150批。营销扩展第一批：迁移 Banner 管理、小程序展示与点击量统计。客服、装修、分销与 App 活动聚合仍未开始。

## 后端

- 新增 `mall_promotion_banner`（`V25__promotion_banner.sql`）：标题、跳转链接、图片、排序、状态、位置 1–5、备注、点击量。
- 后台接口 `/mall/promotion/banners`：分页、详情、新增、修改、删除，沿用 `mall:promotion:*` 权限；修改保留点击量。
- 小程序接口 `/mall/app/banners?position=`：只返回开启状态，按排序值和编号倒序；`PUT /mall/app/banners/{id}/browse` 原子累加点击量。
- 网关 `AuthFilter` 增加精确公开规则：Banner 列表与点击量接口可匿名访问，其他 Banner 写接口仍需会员令牌。
- 位置沿用源定义：1 首页、2 秒杀、3 拼团/砍价、4 限时折扣、5 满减送。

## 后台与小程序

- 新增 `BannerPage` React 页面与 `mall/promotion/banner/index` 路由；菜单种子 `V20261101__mall_banner_menu.sql` 可重复执行且不授予角色。
- 小程序首页读取并展示首页 Banner；站内链接直接跳转，外链复制并提示。

## 验证

- mall 全模块 **171 项**测试通过，0 失败/错误/跳过。
  - `BannerPersistenceTest` 3：位置/状态过滤、排序、分页、修改保留点击量、删除与非法位置。
  - `BannerMenuSeedTest` 1：菜单可重入、不覆盖既有菜单、不分配角色。
  - `ControllerMappingTest` 纳入 7 个新路由。
- React **297 项**、小程序 **162 项**测试通过；新增 Banner API/页面/小程序测试 7 项。
- 真实环境：后台创建 Banner `2116676420497022978`，小程序接口返回开启 Banner；点击两次后 `browse_count=2`；停用 Banner 不出现在小程序列表，后台按状态可查；重建网关后匿名访问列表与点击接口均成功，点击量增至 3。

证据：`evidence/banner-verification.json`。

## 未覆盖

- 客服会话与消息、装修模板、分销关系/佣金/提现、App 活动聚合。
- 浏览器后台与小程序真机联调；本批只验证接口、页面逻辑与数据库状态。
- 未 commit/push。
