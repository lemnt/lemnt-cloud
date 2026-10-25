# 会员后台与小程序页面

v6 · 2026-10-25 · 基础 a261ec3。接口依据 [member-api.md](member-api.md)，设计与验证见 work-items/yudao-member-mall-trade-migration。页面接入真实 HTTP 契约；测试的模拟响应不代表已连接部署环境。

## React 管理后台

沿用 lemnt-ui 的 React、Ant Design、Pro Components、TanStack Query、统一 request 和 usePermission，不引入依赖或新后台项目。

| 菜单组件键 | 页面 | 操作及权限 |
| --- | --- | --- |
| member/user/index | 会员列表与详情 | 手机号筛选、分页；member:user:query；地址另需 member:address:query |
| member/user/index | 会员维护 | 新建 create、编辑 update、重置密码 reset-password；具体前缀 member:user: |
| member/user/index | 资产/归属 | update-point 调积分；update-level 调经验/等级；update 改分组/标签；还须对应元数据 query 权限 |
| member/level/index | 会员等级 | member:level:query/create/update/delete |
| member/group/index | 分组 | member:group:query/create/update/delete |
| member/tag/index | 标签 | member:tag:query/create/update/delete |
| member/record/index | 积分/经验/等级/签到记录 | 与全局记录接口一致：member:point:query、member:experience:query、member:level:query、member:signin:query |
| member/config/index | 积分配置/签到规则 | member:config:query/update；member:sign-in-config:query/update/delete |

积分/经验调整使用有符号变更量，不接收绝对余额。等级调整会重置经验；清空等级重置为0。模态框内的同一操作保留 requestNo；首次提交后冻结内容，失败重试沿用原请求；结果未确认时先核验流水，避免关闭后误开新操作。后台不持久化密码、收货地址或调整载荷到浏览器存储。资料更新不直接改手机，也不代表联系方式验证。

已提交成功与列表刷新失败分开提示。查询失败有明确重试，写失败保留表单；未授权查询不启动，动作也检查权限。新建会员只建资料，不自动签发登录会话。元数据由现有列表接口加载，ProTable 在本地分页；记录和会员列表为服务端分页。行政区划只展示接口 areaId，没有编造区县名称。超出 JavaScript 安全整数范围的资产数值明确拒绝，防止错误展示或舍入后写入。

审核后在系统库串行运行 `lemnt-server/sql/V20261025__member_menu.sql`，脚本可重复执行，不覆盖既有菜单且不授予角色。按实际角色赋予菜单及按钮权限。须使用保留 `/member` 前缀的网关路由，并先部署会员 V2/V3 和权威会话接口。这里没有执行线上脚本或角色授权。

## 原生小程序

在微信开发者工具导入 `lemnt-miniapp`，按实际环境配置 appId 和合法 request 域名。`config.js` 中 apiBaseUrl 必须为合法 HTTPS 地址；memberApiReady 默认仍 false。完成会员服务、网关和供应商环境验证后由部署人员启用，页面代码不擅自打开业务开关。

| 页面路径 | 功能 |
| --- | --- |
| pages/member-login/index | 密码/短信登录、短信找回密码、微信手机号授权登录 |
| pages/member/index | 资料、性别/生日、密码修改、短信/微信换绑手机、微信绑定/解绑、刷新会话/退出 |
| pages/member-addresses/index | 地址列表、新增/编辑、默认地址、确认删除 |
| pages/member-benefits/index | 积分/经验余额、等级权益、签到规则/汇总/签到、积分/经验/签到分页记录 |
| pages/index/index | 保留购物车/结算流程，新增会员中心入口 |

独立会员会话只保存在 app globalData 内存，不写密码、验证码、微信 code 或令牌到持久存储，不借用管理员令牌。小程序重启需要重新登录；可在会员中心显式刷新会话。刷新结果不确定时清除旧会话并要求重新登录，不自动重放业务写操作。密码/手机号变更后退出；退出先清本地状态，服务端撤销失败仍显示错误。成功短信/微信验证的首次登录由服务端建会员。

进入个人页面重新加载本人数据；账号切换期间返回的旧响应被拒绝，页面清空。返回购物车清除旧行/报价/订单显示，但不删除 checkoutFlow 已持久化的会员专属未确认结算意图。换号后的结算不能冒用旧会员。签到只有服务端确认后才显示奖励；保存/签到成功而刷新失败时明确提示，避免重复操作。

地址表单的区县行政区划代码直接对应 areaId；当前服务没有区划字典接口，页面要求填写真实代码及完整省市区/街道/门牌，不用地区名称推算 ID。地址簿仍未接入订单收货地址快照。等级折扣和积分交易配置展示/管理不代表已接入订单定价或抵扣。

微信登录需服务端 `member.wechat.mini.*` 和小程序真实权限；短信需 `member.sms.*`。所有临时 code 只发服务端兑换，不在客户端生成身份。拒绝微信手机号授权不会发起登录。短信每次发送60秒限制仍以服务端为准，前端倒计时仅提供反馈。尚未设置密码的手机号账户可从登录页走短信找回。

## 验证与限制

命令（使用项目要求 Node22.13.0 / Yarn1.22.22）：lemnt-ui 的 typecheck、lint、format:check、test、build；原生 `node --test lemnt-miniapp/tests/*.test.cjs`，以及 JS 语法/JSON/WXML结构和事件绑定检查。完整结果保存在 evidence/member-pages/，后台菜单种子另有 H2 重复执行测试。

浏览器运行环境缺少 Chromium，下载所得内容无效，未取得浏览器交互/视觉 PASS。原生 WXML 编译、微信开发者工具/真机、真实短信/微信和部署后的 MySQL/Redis/Nacos/Dubbo 链路尚未实测。静态检查不等于原生编译；SSR/模拟 HTTP 测试不等于浏览器或后端联调。PR 继续 Draft，不标记生产可用。
