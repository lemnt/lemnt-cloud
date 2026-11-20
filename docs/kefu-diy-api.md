# 客服与装修接口

2026-11-01。客服一个会员一个会话；装修模板属性与页面属性均为 JSON 文本，小程序公开读取装修配置。

## 客服

| 方法与路径 | 身份/权限 | 功能 |
| --- | --- | --- |
| GET /mall/app/kefu/conversation | 会员 | 本人会话；无会话返回 data 为空 |
| GET /mall/app/kefu/messages?limit=50 | 会员 | 最近消息，按时间正序 |
| POST /mall/app/kefu/messages | 会员 | 发送文本（contentType=1）或图片地址（2），无会话自动创建 |
| PUT /mall/app/kefu/read | 会员 | 标记客服消息已读 |
| GET /mall/promotion/kefu-conversations | mall:promotion:list | 管理端会话列表，置顶优先 |
| GET /mall/promotion/kefu-conversations/{id} | mall:promotion:query | 会话详情 |
| GET /mall/promotion/kefu-conversations/{id}/messages | mall:promotion:query | 会话消息 |
| POST /mall/promotion/kefu-conversations/{id}/messages | mall:promotion:create | 客服回复 |
| PUT /mall/promotion/kefu-conversations/{id}/read | mall:promotion:update | 管理员已读并清零未读 |
| PUT /mall/promotion/kefu-conversations/{id}/pinned | mall:promotion:update | 置顶/取消置顶 |
| DELETE /mall/promotion/kefu-conversations/{id} | mall:promotion:delete | 仅后台隐藏会话 |

会员发送时管理员未读数加一；任一方读取自己收到的未读消息后对方消息变为已读。管理员删除会话后，会员再次发送会自动恢复。当前没有 WebSocket，客户端轮询刷新。

## 装修

| 方法与路径 | 身份/权限 | 功能 |
| --- | --- | --- |
| GET /mall/promotion/diy-templates | mall:promotion:list | 模板分页 |
| GET /mall/promotion/diy-templates/{id} | mall:promotion:query | 模板详情 |
| POST /mall/promotion/diy-templates | mall:promotion:create | 新建模板，自动创建首页/我的 |
| PUT /mall/promotion/diy-templates/{id} | mall:promotion:update | 修改名称/备注/预览图 |
| PUT /mall/promotion/diy-templates/{id}/use | mall:promotion:update | 启用模板，保证只有一个 |
| PUT /mall/promotion/diy-templates/{id}/property | mall:promotion:update | 更新模板 JSON 属性 |
| DELETE /mall/promotion/diy-templates/{id} | mall:promotion:delete | 使用中模板不能删除 |
| GET /mall/promotion/diy-pages | mall:promotion:list | 页面分页，可按模板筛选 |
| GET /mall/promotion/diy-pages/{id} | mall:promotion:query | 页面详情 |
| POST /mall/promotion/diy-pages | mall:promotion:create | 新建页面，模板内名称唯一 |
| PUT /mall/promotion/diy-pages/{id} | mall:promotion:update | 修改页面基础信息 |
| PUT /mall/promotion/diy-pages/{id}/property | mall:promotion:update | 更新页面 JSON 属性 |
| DELETE /mall/promotion/diy-pages/{id} | mall:promotion:delete | 删除页面 |
| GET /mall/app/diy/template/used | 公开 | 使用中模板及其首页/我的属性 |
| GET /mall/app/diy/template/{id} | 公开 | 指定模板及首页/我的属性 |
| GET /mall/app/diy/page/{id} | 公开 | 指定页面属性 |

属性必须是 JSON 对象或数组，最多 60000 字。小程序装修页当前支持 Carousel、ImageBar、TitleBar、NoticeBar、MenuGrid、Divider，其他组件显示暂不支持。
