# 支付应用与渠道配置设计

2026-10-31 第136批，责任 code-software-engineer。对应完整迁移 F05 的支付应用/渠道配置部分与台账 Y02/Y03。参考本地芋道 pay 模块的 `PayAppDO`、`PayChannelDO`，保留项目六层 CQRS、逐表 DO/Gateway/Mapper 与 REST 约定，不引入真实渠道 SDK。

## 范围

- 支付应用：分页、详情、新增、修改、启用停用、删除。
- 支付渠道配置：按应用配置渠道编码、状态、费率、备注、JSON 配置；分页、详情、新增、修改、删除、按应用查询。
- 后台 React 页面与菜单/按钮权限种子。
- 明确不在本批：支付通知任务/日志/重试（涉及支付回调路径，单独一批设计）、真实渠道调用、钱包/充值/转账。

## 模型与约束

### 支付应用 trade_pay_app

- 字段：app_key（唯一，创建后不可修改）、name、status、remark、order_notify_url、refund_notify_url、审计列、逻辑删除。
- 状态口径沿用项目：0 启用、1 停用。
- app_key 4-64 位字母/数字/下划线/中划线；回调地址可选，仅接受 http/https。
- 删除前若应用下仍有渠道配置则拒绝。

### 支付渠道配置 trade_pay_channel

- 字段：app_id、code、status、fee_rate、remark、config、审计列、逻辑删除；唯一 (app_id, code)。
- code 只接受 `PaymentChannel` 枚举（ALIPAY/WECHAT），避免存入无法识别的渠道。
- fee_rate 为百分比 0-100，两位小数；config 为不透明 JSON 对象文本，最长 4000。
- 响应**不返回 config 明文**，只返回 `configConfigured`；编辑时 config 留空表示保留原值，填写则整体替换。
- app_id/code 创建后不可修改。

## 接口

支付应用 `/trade/admin/pay-apps`：

| 方法 | 路径 | 权限 |
|---|---|---|
| GET | ``（appKey/name/status/pageNum/pageSize） | trade:pay-app:list |
| GET | `/{id}` | trade:pay-app:query |
| POST | `` | trade:pay-app:create |
| PUT | `/{id}` | trade:pay-app:update |
| PUT | `/{id}/status` | trade:pay-app:update |
| DELETE | `/{id}` | trade:pay-app:remove |

支付渠道配置 `/trade/admin/pay-channels`：

| 方法 | 路径 | 权限 |
|---|---|---|
| GET | ``（appId/code/status/pageNum/pageSize） | trade:pay-channel:list |
| GET | `/{id}` | trade:pay-channel:query |
| GET | `/by-app/{appId}` | trade:pay-channel:query |
| POST | `` | trade:pay-channel:create |
| PUT | `/{id}` | trade:pay-channel:update |
| DELETE | `/{id}` | trade:pay-channel:remove |

## 分层

- 领域：`PayApp`、`PayChannelConfig`、筛选与 Gateway。
- 基础设施：DO/Mapper/Converter/GatewayImpl，MyBatis-Plus 分页与逻辑删除。
- 应用：DTO/命令/查询/执行器，`PayConfigRules` 统一校验。
- 适配：独立 Req/Res 与转换器，两个后台控制器。
- 前端：`payConfigApi` 严格解码（大编号、状态、费率两位小数、configConfigured），`PayConfigPage` 两个页面，菜单种子 `V20261031__trade_pay_config_menu.sql`。

## 验证

- 应用层：校验、唯一性、删除引用保护、密钥留空保留/显式替换。
- 持久化：生产 SQL/DO/Mapper/Gateway + H2，验证唯一、逻辑删除、config 往返、费率精度。
- 路由映射：`ControllerMappingTest` 收录全部新路径与权限。
- 菜单种子：可重复执行、不覆盖既有菜单、不分配角色。
- 前端：API 严格解码与请求字段、类型检查、lint、格式。
- 不执行真实 MySQL/Nacos/Redis/Dubbo/浏览器联调。
