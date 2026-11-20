# 客服与装修模板迁移交付

2026-11-01 第151批。营销扩展第二批：迁移客服会话/消息与装修模板/页面，包含后台管理页、小程序客服与装修页。分销与 App 活动聚合仍未开始。

## 客服

- 新增 `mall_promotion_kefu_conversation`、`mall_promotion_kefu_message`（`V26__promotion_kefu.sql`）；一个会员一个会话，保存最后消息、置顶、删除标记和管理员未读数。
- 小程序：`GET /mall/app/kefu/conversation`、`GET/POST /mall/app/kefu/messages`、`PUT /mall/app/kefu/read`；发送时自动创建会话，支持文本和图片地址。
- 后台：`/mall/promotion/kefu-conversations` 列表/详情/消息/回复/已读/置顶/删除；回复使用 `mall:promotion:create`，已读与置顶使用 `mall:promotion:update`。
- 会员发送消息时管理员未读加一；管理员已读后清零；管理员删除只对后台隐藏，会员再次发送自动恢复。
- 未接入 WebSocket，后台与小程信用轮询刷新（小程序 5 秒）。

## 装修

- 新增 `mall_promotion_diy_template`、`mall_promotion_diy_page`（`V27__promotion_diy.sql`）；属性保存 JSON 文本，模板有且仅有一个 `used=1`。
- 后台：`/mall/promotion/diy-templates` 分页/详情/新增/修改/启用/删除/属性更新；`/mall/promotion/diy-pages` 分页/详情/新增/修改/删除/属性更新。
- 新建模板自动创建“首页”“我的”两个页面；模板内页面名称唯一；使用中模板不能删除。
- 小程序：`GET /mall/app/diy/template/used`、`/template/{id}`、`/page/{id}`，公开读接口。
- 小程序装修页支持 Carousel、ImageBar、TitleBar、NoticeBar、MenuGrid、Divider；其他组件显示“暂不支持组件”，避免静默渲染错误。

## 后台与菜单

- 新增 `KefuPage`、`DiyPageView` React 页面及路由 `mall/promotion/kefu/index`、`mall/promotion/diy/index`。
- 菜单种子 `V20261101__mall_kefu_menu.sql`、`V20261101__mall_diy_menu.sql`，可重复执行且不授予角色。
- 网关公开规则增加装修模板与页面读取；客服接口仍需会员令牌。

## 验证

- mall 全模块 **181 项**测试通过，0 失败/错误/跳过；新增 `KefuPersistenceTest` 4、`DiyPersistenceTest` 5、`KefuDiyMenuSeedTest` 1。
- React **304 项**、小程序 **166 项**、网关 **35 项**测试通过。
- 真实环境：
  - 客服会话 `2116687669322543105`：会员发送后管理员未读 1，后台回复后会员未读 1，双方已读后清零；置顶、后台删除隐藏、会员再次发送恢复均验证。
  - 装修模板 `2116687774633127938`：自动生成“首页/我的”，首页属性写入 Carousel/TitleBar 后小程序公开接口返回；启用切换只有一个模板 `used=true`；删除使用中模板被拒绝。

证据：`evidence/kefu-diy-verification.json`。

## 未覆盖

- 客服 WebSocket 实时推送、图片上传、语音/视频与商品/订单消息。
- 装修全部源组件（商品列表/秒杀/拼团/积分/热区/视频等）与可视化拖拽编辑器；目前属性使用 JSON 文本编辑。
- 浏览器后台与小程序真机联调。
- 本批未 commit/push。
