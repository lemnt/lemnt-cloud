# member 结构与规范整理

输入：分析并调整 member 项目结构、代码规范和域划分，参考 system；删除短信和微信逻辑；修改后不提交。

- [分析与开发设计](03-development-design.md)
- [实现与验证](05-development.md)

保留既有 app/web、认证位于 auth、公共 TokenService/LOGIN_TOKEN_KEY、网关无 Dubbo、无会员全设备会话索引的决定。

- [商城复刻基线](22-mall-replication-baseline.md)
- [配送基础批次设计](23-delivery-foundation-design.md)
- [配送基础批次交付](24-delivery-foundation-delivery.md)
- [配送管理接口](../../docs/trade-delivery-api.md)

- [配送结算贯通设计](25-delivery-checkout-design.md)
- [配送结算贯通交付](26-delivery-checkout-delivery.md)
- [配送结算接口](../../docs/delivery-checkout-api.md)

- [完整迁移设计与清单](27-full-migration-design.md)
- [优惠券快照迁移设计](28-full-coupon-design.md)
- [优惠券当前接口](../../docs/coupon-migration-api.md)

- [订单日志迁移设计](29-order-log-design.md)
- [订单日志及本人操作接口](../../docs/order-operation-log-api.md)

- [优惠券与订单日志实现及验证](30-coupon-order-delivery.md)

- [售后标准表与日志设计](31-aftersale-log-design.md)
- [售后迁移交付与验证](32-aftersale-log-delivery.md)
- [售后当前接口](../../docs/after-sale-log-api.md)

- [原订单商品快照设计](33-order-product-snapshot-design.md)
- [商品快照交付与验证](34-order-product-snapshot-delivery.md)
- [订单与售后快照接口](../../docs/order-product-snapshot-api.md)

- [商品规格字典关联设计](35-product-spec-identity-design.md)
- [规格字典关联交付](36-product-spec-identity-delivery.md)
- [规格关联接口](../../docs/product-spec-identity-api.md)

- [完整规格快照列表设计](37-property-snapshot-list-design.md)
- [规格列表贯通交付](38-property-snapshot-list-delivery.md)

- [商品规格列表合同设计](39-product-property-contract-design.md)
- [商品规格合同收敛交付](40-product-property-contract-delivery.md)

- [SKU规格存储合并设计](41-sku-property-storage-design.md)
- [SKU JSON存储合并交付](42-sku-property-storage-delivery.md)

- [商品收藏与浏览足迹设计](43-product-interaction-design.md)
- [商品收藏与浏览足迹交付](44-product-interaction-delivery.md)
- [商品收藏与足迹接口](../../docs/product-interaction-api.md)

- [文章相关迁移范围调整](45-article-scope-adjustment.md)

- [商品评价链路设计](46-product-comment-design.md)
- [商品评价链路交付](47-product-comment-delivery.md)
- [商品评价当前接口](../../docs/product-comment-api.md)

- [订单项评价联动设计](48-order-item-comment-design.md)
- [订单项评价联动交付](49-order-item-comment-delivery.md)
- [订单项评价接口](../../docs/order-item-comment-api.md)

- [后台评价筛选设计](50-comment-filter-design.md)
- [后台评价筛选交付](51-comment-filter-delivery.md)

- [商品评价源差异复核及清单纠正](52-comment-source-audit.md)

- [后台自评设计](53-comment-self-design.md)
- [后台自评交付](54-comment-self-delivery.md)

- [收货时间迁移设计](55-order-receive-time-design.md)
- [收货时间迁移交付](56-order-receive-time-delivery.md)

- [系统评价调度设计](57-system-comment-design.md)
- [系统评价调度交付](58-system-comment-delivery.md)

- [同页多商品评价设计](59-order-comment-page-design.md)
- [同页多商品评价交付](60-order-comment-page-delivery.md)

- [评价图片上传设计](61-comment-upload-design.md)
- [评价图片上传交付](62-comment-upload-delivery.md)

- [评价图片交互设计](63-comment-image-interaction-design.md)
- [评价图片交互交付](64-comment-image-interaction-delivery.md)

- [会员后台备注设计](65-member-mark-design.md)
- [会员后台备注交付](66-member-mark-delivery.md)

- [会员姓名地区设计](67-member-identity-design.md)
- [会员姓名地区交付](68-member-identity-delivery.md)

- [会员地区选择设计](69-member-area-design.md)
- [会员地区选择交付](70-member-area-delivery.md)

- [会员登录审计时间设计](71-member-login-audit-design.md)
- [会员登录审计时间交付](72-member-login-audit-delivery.md)

- [会员登录连接IP设计](73-member-login-ip-design.md)
- [会员登录连接IP交付](74-member-login-ip-delivery.md)

- [后台签到昵称筛选设计](75-sign-record-nickname-design.md)
- [后台签到昵称筛选交付](76-sign-record-nickname-delivery.md)

- [后台积分流水筛选设计](77-point-record-filter-design.md)
- [后台积分流水筛选交付](78-point-record-filter-delivery.md)

- [积分业务元数据快照设计](79-point-record-metadata-design.md)
- [积分业务元数据快照交付](80-point-record-metadata-delivery.md)

- [积分数值类型筛选设计](81-point-numeric-filter-design.md)
- [积分数值类型筛选交付](82-point-numeric-filter-delivery.md)

- [积分独立表设计](83-point-table-design.md)
- [积分独立表交付](84-point-table-delivery.md)

- [经验独立表设计](85-experience-table-design.md)
- [经验独立表交付](86-experience-table-delivery.md)

- [泛化资产网关退役设计](87-asset-gateway-retirement-design.md)
- [泛化资产网关退役交付](88-asset-gateway-retirement-delivery.md)

- [流水基础审计设计](89-ledger-audit-design.md)
- [流水基础审计交付](90-ledger-audit-delivery.md)

- [等级记录快照设计](91-level-record-snapshot-design.md)
- [等级记录快照交付](92-level-record-snapshot-delivery.md)

- [等级自动变更规则设计](93-level-transition-design.md)
- [等级自动变更规则交付](94-level-transition-delivery.md)

- [经验流水筛选设计](95-experience-record-filter-design.md)
- [经验流水筛选交付](96-experience-record-filter-delivery.md)

- [等级记录筛选设计](97-level-record-filter-design.md)
- [等级记录筛选交付](98-level-record-filter-delivery.md)

- [积分记录剩余筛选设计](99-point-record-filter-design.md)
- [积分记录剩余筛选交付](100-point-record-filter-delivery.md)

- [签到记录时间及筛选设计](101-sign-record-time-design.md)
- [签到记录时间及筛选交付](102-sign-record-time-delivery.md)

- [签到执行规则设计](103-sign-flow-design.md)
- [签到执行规则交付](104-sign-flow-delivery.md)

- [签到配置ID管理设计](105-sign-config-id-design.md)
- [签到配置ID管理交付](106-sign-config-id-delivery.md)

- [积分配置闭环设计](107-point-config-design.md)
- [积分配置闭环交付](108-point-config-delivery.md)

- [会员等级管理收敛设计](109-level-management-design.md)
- [会员等级管理收敛交付](110-level-management-delivery.md)

- [分组标签管理收敛设计](111-group-tag-management-design.md)
- [分组标签管理收敛交付](112-group-tag-management-delivery.md)

- [会员列表源筛选设计](113-member-user-filter-design.md)
- [会员列表源筛选交付](114-member-user-filter-delivery.md)

- [后台会员关系返显设计](115-member-user-relations-design.md)
- [后台会员关系返显交付](116-member-user-relations-delivery.md)

- [后台会员资料保存设计](117-member-user-update-design.md)
- [后台会员资料保存交付](118-member-user-update-delivery.md)

- [停用会员撤销会话设计](119-member-disable-session-design.md)
- [停用会员撤销会话交付](120-member-disable-session-delivery.md)

- [同等级手工调整设计](121-member-level-noop-design.md)
- [同等级手工调整交付](122-member-level-noop-delivery.md)

- [收货地址源逻辑收敛设计](123-member-address-alignment-design.md)
- [收货地址源逻辑收敛交付](124-member-address-alignment-delivery.md)

- [收货地址地区名称设计](125-member-address-area-name-design.md)
- [收货地址地区名称交付](126-member-address-area-name-delivery.md)

- [迁移完成率重新统计（当前口径）](127-migration-progress-recount.md)

- [前端前缀与网关链路交付](128-service-prefix-delivery.md)

- [促销与支付五个后台入口交付](129-backoffice-management-pages-delivery.md)

2026-10-31 第130批：补齐快递公司、运费模板、交易配置三个后台入口；复用现有接口、服务前缀、菜单权限，保留子规则身份与未开放配置原值。交付见[130-delivery-config-management-pages-delivery.md](130-delivery-config-management-pages-delivery.md)，不将代码/SSR/H2证据等同真实联调。

2026-10-31 第131批：规格、规格值后台管理及商品编辑字典刷新已接通；补齐真实创建时间、规格项时间范围筛选和源编号倒序。无业务表变更，菜单不自动授权；见[131-product-spec-management-delivery.md](131-product-spec-management-delivery.md)。

2026-10-31 第132批：商品页接通回收/恢复/删除、三种真实状态计数、分类品牌筛选与权限；260项前端、32项后端聚焦测试通过。售罄/警戒库存、经营字段及导出仍待补齐；见[132-spu-lifecycle-management-delivery.md](132-spu-lifecycle-management-delivery.md)。

2026-10-31 第133批：补齐真实SKU库存汇总的售罄/警戒库存分组与数量、当前筛选XLSX导出及独立权限。265项前端、38项后端聚焦验证通过；经营字段和真实联调仍待补。见[133-spu-stock-tabs-export-delivery.md](133-spu-stock-tabs-export-delivery.md)。

2026-10-31 第134批：关键字、简介、轮播图和排序贯通表/各层/后台/导出；新增旧库字段升级脚本但未执行真实库。266项前端、mall全部31类132项测试通过；上传交互与更多经营/审计字段待补，见[134-spu-operating-fields-delivery.md](134-spu-operating-fields-delivery.md)。

2026-10-31 第135批：App订单中心与售后中心闭环。后端新增本人订单分页/状态数量/详情/物流轨迹、本人售后分页/详情/额度/申请/登记退货，SQL按member_id过滤且他人资源统一不存在；小程序新增订单列表、订单详情、售后列表、售后申请、售后详情五页。trade各模块174项、小程序136项测试通过；真实环境、收银台/渠道SDK与真机联调仍待完成，见[135-app-order-aftersale-center-delivery.md](135-app-order-aftersale-center-delivery.md)。

2026-10-31 第136批：支付应用与支付渠道配置。新增trade_pay_app/trade_pay_channel两表及CRUD、后台两页与菜单种子；渠道config明文不出接口、留空保留原值、code限定ALIPAY/WECHAT。trade全模块185项、前端271项测试通过；支付通知任务/日志/重试涉及回调路径，留待下一批，见[136-pay-app-channel-delivery.md](136-pay-app-channel-delivery.md)。

2026-10-31 第137批：支付通知任务/日志/重试。新增派发任务与尝试日志两表、CAS尝试与退避重试、调度器与后台查询/重发；PAY派发库存核券、REFUND派发权益回退，失败仍向上抛出并保留任务。trade全模块196项（含100项下单集成）、前端274项测试通过；调度默认关闭、真实渠道与部署联调未做，见[137-pay-notify-task-delivery.md](137-pay-notify-task-delivery.md)。

2026-10-31 第138批：会员钱包账户与流水。新增trade_member_wallet/transaction两表，行锁串行资金变更、业务幂等、冻结/解冻、后台人工调整与App查询；金额元DECIMAL(19,2)。trade全模块206项、前端277项、小程序138项测试通过；充值套餐/转账与真实环境联调未做，见[138-wallet-account-ledger-delivery.md](138-wallet-account-ledger-delivery.md)。

2026-10-31 第139批：充值套餐与充值单。新增套餐与充值单两表、套餐CRUD、按套餐下单（requestNo幂等）、后台确认到账入钱包、退款出钱包；后台两页与小程序充值页。trade全模块215项、前端280项、小程序140项测试通过；真实渠道/异步退款与转账未做，见[139-wallet-recharge-delivery.md](139-wallet-recharge-delivery.md)。

2026-10-31 第140批：钱包转账。新增转账表，钱包间同步互转（requestNo幂等、双钱包按编号升序加锁、双向TRANSFER流水）；后台只读转账页与小程序转账页。trade全模块222项、前端281项、小程序142项测试通过；真实渠道出款与异步状态未做，见[140-wallet-transfer-delivery.md](140-wallet-transfer-delivery.md)。

2026-10-31 第141批：促销运营四子域（秒杀/拼团/砍价/积分商城）。新增11张活动/记录表，完成后台管理与小程序浏览/参团/助力/积分页、时段冲突、库存限购、并发助力、拼团过期/虚拟成团等规则。mall全模块154项、前端292项、小程序153项测试通过；活动下单与trade订单处理器、取消退款恢复及真实环境联调未做，见[141-promotion-operations-delivery.md](141-promotion-operations-delivery.md)。

2026-10-31 第142批：促销活动订单集成。新增活动订单映射与退款去重两表、四个活动下单接口，交易生命周期回调接入活动库存预占/恢复、拼团支付建团、砍价订单绑定；mall全模块165项、前端292项、小程序155项测试通过。0元积分兑换、多SKU/非默认地址、拼团整单退款失败团及真实环境联调未做，见[142-promotion-order-integration-delivery.md](142-promotion-order-integration-delivery.md)。

2026-10-31 第143批：0元积分兑换与交易零支付。交易允许0元订单（需消耗积分），库存与活动预占成功后自动PAID并确认库存；活动订单允许0元快照；会员0现金实付扣/退积分且不产生经验。mall 165项、trade 224项、member 118项、前端292项、小程序155项测试通过，见[143-zero-cash-point-order-delivery.md](143-zero-cash-point-order-delivery.md)。

2026-10-31 第144批：0元订单售后退款与积分回退。售后/退款单允许无支付单和0金额，零元退款以POINT成功记录派发权益任务；商城恢复活动库存并置活动订单REFUNDED，会员整单退积分。trade 227项、mall 165项、member 118项、前端292项、小程序155项测试通过，见[144-zero-cash-aftersale-refund-delivery.md](144-zero-cash-aftersale-refund-delivery.md)。
