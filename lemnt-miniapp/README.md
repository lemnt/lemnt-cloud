# lemnt 微信小程序

独立于 React 管理后台的微信原生项目。微信开发者工具导入本目录，配置自己的 AppID；仓库中的 touristappid 只供项目结构导入。无 npm 运行依赖，Node 22 执行 `npm test` 验证请求边界。

当前交付：项目配置、可打开的接入状态页、可注入传输层的交易客户端和边界测试。**不是可下单的小程序成品**：会员登录、用户端后端接口及微信支付尚未接通，因此 config.js 默认关闭网络业务。未运行微信开发者工具或真机测试。

## 接口边界

以下是待后端实现的契约，不是已部署接口：

| 方法 | 路径 | 输入 |
| --- | --- | --- |
| GET | /trade/app/cart | 无 |
| PUT | /trade/app/cart/{productId} | quantity、selected |
| DELETE | /trade/app/cart/{productId} | 无 |
| GET | /trade/app/coupons | 无 |
| POST | /trade/app/checkout/quote | productIds、可选 couponId |
| POST | /trade/app/checkout | 上述字段、requestNo、quoteToken |
| GET | /trade/app/orders/{orderNo} | 无 |
| POST | /trade/app/orders/{orderNo}/prepay | 无客户端金额 |
| GET | /trade/app/orders/{orderNo}/payment | 无 |

响应沿用 R 包装。金额必须保持十进制字符串，商品及会员等 Long ID 在前端统一字符串。服务端必须通过独立会员会话确定 memberId，校验订单/券归属和会员有效性，不能接收客户端指定身份，也不能把后台 SecurityUtils.getUserId() 当作会员ID。网关须拒绝后台令牌访问会员接口，并移除客户端伪造身份头。

已有 /trade/admin/** 和 /trade/orders 是后台权限接口；本客户端不调用它们。现有后台购物车和结算页面视为历史管理工具，不计入小程序迁移完成度。商品、券模板、发货、审核、补偿留在后台。支付渠道回调另设服务端入口，验签后才进入支付应用服务，不使用小程序会话。

## 接通顺序

1. 实现独立会员登录/退出/过期会话及网关身份隔离，微信登录凭证仅在服务器交换，不在客户端保存商户密钥。
2. 实现以上用户端入口，复用现有计价、订单、库存和 Outbox 服务；补充跨会员越权与后台令牌拒绝测试。
3. 实现真实微信预支付和回调验签，响应签名支付参数；客户端 wx.requestPayment 返回成功只代表客户端调用结果，最终状态必须查询服务器。当前项目没有虚构支付参数或调用假成功。
4. 结算 UI 必须在发出请求前持久化会员会话隔离的不可变 requestNo/quoteToken/商品载荷，未知结果用原载荷重试；退出/换号不得重放另一个会员的请求。当前服务客户端只接受调用方提供的稳定编号，尚未提供完整结算页面和持久化工作流。
5. 配置合法 HTTPS 请求域名、AppID 和 config.js，完成开发者工具/真机验证后才打开 memberApiReady。不得为了调试关闭域名校验后宣称可发布。
