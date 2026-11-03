# lemnt 微信小程序

独立于 React 管理后台的微信原生项目。微信开发者工具导入本目录，配置自己的 AppID；仓库中的 touristappid 只供项目结构导入。无 npm 运行依赖，Node 22 执行 `npm test` 验证请求边界。

当前交付：商品浏览/搜索/详情/规格与幂等加购、领券中心/钱包、会员页面及购物车/服务端缓存结算页面和对应接口代码。启动页为商品首页。真实部署、微信支付及原生编译/真机尚未核验，config.js 仍默认关闭网络业务，不能标为生产成品。详见 [商品与优惠券接口](../docs/storefront-marketing-api.md) 和 [会员页面](../docs/member-pages.md)。

## 当前接口边界与验证

2026-11-03：Node测试179项通过，JS/JSON/WXSS格式检查和38个WXML标签缩进检查通过。统一规范见[AGENTS.md](AGENTS.md)及[代码规范](../docs/code-standards.md)，完整范围见[第165批交付](../work-items/member-domain-structure/165-project-code-standards-delivery.md)。业务开关仍默认关闭，真实部署、微信原生编译、渠道支付和真机验收均未完成。

当前路由由services/trade.js显式区分mall与trade，不能沿用历史版本的trade购物车前缀：

| 方法 | 当前路径 | 输入/说明 |
|---|---|---|
| GET/PUT/DELETE | /mall/app/cart 及 /mall/app/cart/{productId} | 购物车及数量/选中状态 |
| GET | /mall/app/coupon-center、/mall/app/coupon-wallet | 领券中心及本人优惠券 |
| POST | /mall/app/checkout/quote | 当前商品、配送与可选券，服务端计价 |
| POST | /mall/app/checkout | settlementId、requestNo，不提交金额 |
| GET | /mall/app/checkout/requests/{requestNo} | 按原请求找回结算回执 |
| GET | /trade/app/orders/{orderNo} | 本人订单完整详情 |
| POST | /trade/app/orders/{orderNo}/payment | appKey、channel，不提交金额 |
| GET | /trade/app/orders/{orderNo}/payment | 服务端支付状态 |
| GET | /trade/app/orders/{orderNo}/promotions | 原订单优惠快照 |
| GET | /trade/app/after-sales/{no}/promotions | 冻结退款归因与权益任务状态 |

HTTP保持R包装。金额和Long ID在解码边界保持精度；会员身份来自当前会话，服务端核对订单、券与售后归属，不接受页面输入memberId决定身份。不使用管理员令牌调用会员接口，也不在页面计算退款尾差。

上线前需核对HTTPS域名、AppID、后端路由、会员验证、支付配置及数据库版本，完成微信开发者工具/真机和目标环境验收后再决定开启memberApiReady。旧结算未知结果必须使用原请求查询/重试，不能以清本机记录替代服务端取消。

## 历史批次记录

以下保留各批当时的能力、协议和测试数量，包含后来已经替换的入口与实现缺口描述；不作为当前代码状态。当前请求以services和上述接口表为准，完整迁移进度以work-items目录的最新批次为准。

## 后端入口接入状态

AppCheckoutController 已提供 GET/PUT/DELETE cart、GET coupons、POST checkout/quote、POST checkout、GET orders/{orderNo}。GET orders 当前返回 CheckoutReceiptRes，不是物流详情；非本人或不存在统一返回订单不存在。金额及字段见 docs/trade-checkout-api.md。

所有入口调用独立 MemberSessionService，只读取 Authorization Bearer，不接受 memberId。MemberSessionGatewayImpl 必须有且仅有一个可信 MemberSessionVerifier；目前没有任何真实实现，默认返回未授权。接口不能通过打开 memberApiReady 获得登录能力。Verifier 必须验证会员令牌的 audience、过期及撤销状态，并拒绝后台令牌；尚未进行该真实身份系统的联调。不要以返回固定会员ID的实现接入部署。

现有网关身份链仍服务于后台，尚需实现会员路由的独立验证；本批未改白名单或部署路由。保持 memberApiReady=false。

## 购物车和结算页面续迁

首页现可在真实会员接通后加载购物车、预览已选可售商品并提交订单。此版本只展示购物车，不提供增删改数量/选中操作或选券 UI。会员切换后必须重新加载和预览。提交前按 memberSession.memberId 隔离保存不可变 requestNo/quoteToken/商品载荷，不保存令牌；未知结果用原请求重试，不能自动丢弃损坏记录或生成新请求。仅响应具备合法订单号/状态/金额后清理请求。

页面仍默认关闭，会员登录/支付/微信开发者工具及真机验证未完成。服务端仍拒绝没有真实会员会话验证器的请求。Node 内置测试当前共9项通过，不代表原生 WXML 编译或实际下单通过。

## 购物车编辑与订单刷新

已新增数量加减、勾选/取消、确认删除以及当前订单回执手动刷新。编辑前检查持久化未确认结算记录；存在记录时拒绝编辑。修改请求发出前废弃旧报价，失败时需刷新核验；删除返回 false 不能提示成功。数量 UI 限1..999。刷新订单只接受相同订单编号及已知状态/金额格式，不能通过客户端刷新伪造支付结果。异步响应期间换号会清空旧会员页面数据。订单回执仍仅保存在当前页面，暂无跨页面订单列表/历史记录或自动轮询。

Node 测试15项通过；原生 WXML 编译、开发者工具和真机仍未验证。会员验证器和真实支付未接通，业务开关仍为 false。

## 优惠券选择

支持加载当前会员持有券、选择 AVAILABLE 券及不使用券；换券、刷新券列表或重新请求预览前废弃旧报价。待确认结算请求存在时禁止更改券或刷新券列表，原请求中的券保持不变。券归属、状态、重复编号及大ID精度在边界校验；实际抵扣资格和金额仍由服务端计算。接口尚不含券名称/门槛/有效期，页面仅展示编号与状态并提示预览核验，不编造优惠规则。

小程序Node测试20项通过。业务开关仍关闭；会员身份、真实支付和微信原生编译/真机/视觉验收未完成。

## 缓存结算单提单

最新用户端协议见 docs/cached-settlement-api.md：预览由服务端生成15分钟结算单并写入Redis；小程序只提交 settlementId/requestNo。本机v2记录不保存商品/金额/券载荷，重试不能替换结算单。旧v1未确认记录阻止新提交，不能升级时自动丢弃。页面提供明确确认的本机清除操作，仅用于核验原订单后恢复，不取消服务器订单，也不能保证新结算不生成第二单。服务端依赖Redis及V6关联表，身份验证器仍未接通，业务开关继续关闭。

## 按请求找回订单

可按原requestNo查询本人已提交订单，不访问结算缓存、不重新提单，同时支持v1旧记录。仅查回有效订单后删除被核验的那一条本机记录；查无结果、换号、响应异常或本机记录已变化时保留。查无结果不意味着原请求已经取消或可以安全另下新单。会员身份系统仍未接通，实际联网业务保持关闭。

## 服务端确认终止未完成提单

新增“终止未完成提单”：服务器尚未生成订单时持久化终止标记，阻止同一结算单的延迟提单；已生成订单时返回原订单，不自动关闭订单。只有服务端明确确认终止或返回有效订单后，才清除被核验的v2记录。网络失败/换号保留记录。新版记录禁止仅清除本机，旧版v1记录仍先按请求找回；明确确认的本机清理只影响v1。需后端V7及所有提单实例支持，会员会话和支付仍未接通，业务开关保持关闭。

## 本地支付记录查询

订单回执旁可查询本地支付记录；接口固定 channelVerified=false，未查询真实渠道。金额/状态矛盾或多笔成功会提示对账；本地WAITING不能认定渠道未支付，查询不会自动关单、退款或释放库存。真实预支付与渠道支付仍未接通，业务开关保持false。详见 docs/miniapp-local-payment-status.md。

## 会员页面

新增会员登录、个人中心、地址簿与权益/签到记录页面，购物车顶部可进入。会话仅保存在内存，小程序重启需登录；显式刷新在个人中心。管理员凭据不适用。见 [会员页面说明](../docs/member-pages.md)。配置和真实供应商验证完成前保持 memberApiReady=false；本次不包含真实支付、订单收货地址接入或原生真机验收。
