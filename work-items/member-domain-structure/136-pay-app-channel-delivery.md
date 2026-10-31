# 支付应用与渠道配置交付与验证

2026-10-31 第136批。按设计交付 PayApp 与渠道配置的后端、后台页面与菜单种子。没有引入真实支付渠道 SDK，没有改动支付回调路径与既有支付/退款表。

## 后端

新增 `trade_pay_app`、`trade_pay_channel` 两表（`lemnt-server/lemnt-trade/sql/V10__pay_app_channel.sql`），沿用 BaseDO 审计与逻辑删除。

- 支付应用 CRUD：`/trade/admin/pay-apps`，appKey 唯一且创建后不可修改；回调地址仅 http/https；删除前校验渠道引用。
- 支付渠道配置 CRUD：`/trade/admin/pay-channels`，唯一 (app_id, code)，code 仅接受 ALIPAY/WECHAT，费率 0-100 两位小数。
- 渠道 config 明文不出接口：响应只返回 `configConfigured`；更新时 config 留空保留原值，显式填写整体替换；config 必须为合法 JSON 对象。
- 校验集中在 `PayConfigRules`，应用层执行器返回独立 DTO，适配层使用独立 Req/Res。
- 菜单/按钮种子 `lemnt-server/sql/V20261031__trade_pay_config_menu.sql`，可重复执行，不分配角色。
- `ControllerMappingTest` 已收录本批与上一批 App 新路径；未引入 Dubbo 或网关 RPC 变更。

## 后台

- `modules/trade/payment/api/payConfigApi.ts`：严格解码大编号、状态、费率两位小数、`configConfigured`；创建/更新分别构造请求，编辑渠道不重发 appId/code，config 留空发送 null 以保留原值。
- `modules/trade/payment/pages/PayConfigPage.tsx`：支付应用、支付渠道配置两个 ProTable + AdminModalForm 页面；按 list/query/create/update/remove 权限控制操作。
- 路由注册 `trade/pay-app/index`、`trade/pay-channel/index`，菜单种子指向相同 component。

## 验证

- 后端：Java 17 执行 trade 全模块测试，185 项通过、0 失败/错误/跳过：
  - domain 2、application 29（新增 `PayConfigExeTest` 7）、infrastructure 10、adapter 25、start 119。
  - 新增 `PayConfigPersistenceTest` 3：生产 SQL/DO/Mapper/Gateway + H2，唯一键、逻辑删除、config 往返、费率精度。
  - 新增 `PayConfigMenuSeedTest` 1：菜单可重入、不覆盖原菜单、不改角色。
  - `ControllerMappingTest` 与 `CheckoutFlowIntegrationTest` 通过。
- 前端：`yarn test` 271 项通过（新增 `payConfigApi.test.ts` 5）；`typecheck`、`lint`、`format:check` 通过。
- 证据：`evidence/pay-app-channel-verification.json`。

## 未完成/限制

- **支付通知任务/日志/重试未在本批**：该项需要改动 `PaymentNotifyCmdExe`/退款完成链路并影响下单集成测试语义，单独一批设计，见台账 Y04。
- 未执行真实 MySQL 迁移、Nacos/Redis/Dubbo/网关部署与浏览器联调；H2 与单测不等于真实环境验收。
- 未实现真实支付渠道调用与收银台（按约定后置）；渠道 config 目前为不透明 JSON，生产环境仍需评估密钥加密与访问审计。
- 本批未主动 commit/push。若工作区再次出现非本会话的自动提交，请由用户确认来源。
