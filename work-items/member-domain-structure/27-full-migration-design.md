# 商城完整代码迁移设计与执行清单

2026-10-29 v4（范围调整，原v3实现记录保留），责任 code-software-engineer。用户最新要求“迁移完整代码”，取代此前只完成单批次就结束的范围。参考本地yudao-cloud mall/member/pay完整源码；源文件SHA见evidence/full-migration-manifest.json。参考技能development-design.md/api-documentation.md/java.md及项目AGENTS.md，沿用已确认system风格、六层CQRS、独立Req/Res、逐表Gateway/Mapper/DO、MyBatisPlus/Lambda/XML/resultMap。

完成标准：对应业务表/字段、状态机、事务/幂等、接口、调用方、后台/小程序页面或任务消费者和相关验证同时交付；不以复制源文件、建空表、生成CRUD或构建通过代替完整迁移。真实渠道SDK按已确认要求后置，演示表及已明确去掉的会员短信微信不纳入；不运行真实数据库迁移、不commit/push。用户已提供完整后台源码 `/Users/wangxx/demo/yudao-ui-admin-vue3/`，以其页面、表单、操作、权限及 API 为后台迁移对照，保留当前 React 技术栈；源文件 SHA 和逐项状态见 `evidence/full-frontend-migration-manifest.json`。App 端参考已由用户补充为 `/Users/wangxx/demo/yudao-mall-uniapp/`（uni-app，包含页面、接口和共享组件）；以其商品、购物车、结算、支付、售后、会员及营销交互核对当前客户端迁移，源文件 SHA 和状态见 `evidence/full-app-migration-manifest.json`。参考项目支持多端不代表当前项目已通过 iOS/Android/H5/小程序运行验证；各端分别记录。已明确移除的短信、微信认证不因参考源码存在而恢复，真实渠道 SDK 仍后置。

## 依赖顺序与任务

|任务|范围|主要路径|依赖|验收|
|---|---|---|---|---|
|F01|完整券模板、持券快照、范围/有效期/限领/撤回/过期|mall各层coupon及SQL、营销和结算调用|现有商品会员配送链路|模板修改不改已发券、并发限领、有效期和范围计价|
|F02|完整订单/售后字段、操作日志、调价/改址/取消/收货/评价状态|trade各层order/aftersale/fulfillment及SQL|F01|状态转换事务日志一致、重复调用不重复日志、售后额度|
|F03|商品资料/规格/评价完整、收藏/足迹|mall各层product、app/web及页面|现有product|快照/评价维度、收藏幂等、足迹分页和清理|
|F04|会员资料/审计及积分经验/等级流水对齐，清理旧认证表引用|member各层及auth调用、SQL、页面|现有member|资料和流水一致、余额与等级、密码及地址隔离|
|F05|支付应用/渠道/支付尝试/通知任务及日志、订单退款完整关联|trade payment/paymentrefund/notification|F02|支付/退款状态、可靠通知重试、未接渠道明确失败|
|F06|收藏及配置/快递/模板后台、自提门店和下单核销|trade delivery与mall结算、UI|F02/F03|配送方式校验、门店启用及核销权限|
|F07|秒杀、拼团、砍价、积分商城及订单处理器/任务|mall promotion子域、trade订单RPC及页面|F01/F02/F03|活动库存/限购、参团到期、助力、积分回退及重试|
|F08|分销关系、佣金、冻结解冻、提现及退款回退|mall promotion.brokerage、trade回调|F02/F05|绑定模式、佣金金额、重复回调、冻结和提现状态|
|F09|钱包、账户流水、充值/套餐、转账|trade payment.wallet/payment.transfer及页面|F05|余额不可负、并发幂等、充值退款和冻结转账|
|F10|轮播、装修模板/页面、客服消息会话|mall promotion各子域及app/web/UI|F03/F04|发布/启停、模板启用、消息归属及未读和推送|
|F11|商品/交易统计任务与页面|mall product统计、trade统计|F02/F03/F08/F09|原事实口径、按天重算幂等、退款与赠送不重复|
|F12|全面接口/表/字段/任务/页面对照、淘汰兼容残留、发布脚本及回归|全模块、UI、miniapp、docs|F01-F11|逐项标记PASS/未验证，闭环后再做lemnt定制|

每项独立更新设计细节、实际变更及验证。跨服务只走client契约，Gateway隐藏RPC；订单下单仍同步预占库存和锁券，支付后核券，关闭异步统一回滚；不引入outbox。金额沿用当前元/精确BigDecimal边界并明确参考分单位转换；不以同名字段直接替换不同含义。新表按实际业务域归属映射，不照搬会违反既有服务边界的名字。

## 当前起点与风险

起点44张当前初始化表，参考仍缺38张业务表（另2演示不迁）；已有模型的券快照、订单/售后、商品会员字段仍不完整。上轮245项受影响后端、162前端、74小程序测试是回归起点，不证明本轮完整迁移已完成。服务端参考是源码目录，无Git提交标识，以内容哈希追踪。当前不具备真实MySQL/Redis/Dubbo及供应商端到端验收环境，分层报告测试证据。

## 当前实现进度（2026-10-28）

F01进行中：规则快照、限领/无限量、DATE/TERM、商品范围计价、独立券包、模板编辑/删除、发券分页/撤回、过期任务及核销/退券计数已接入。F02进行中：订单日志及会员取消/收货已接入真实事务，后台详情可查。尚未完成项不能据构建通过标为迁移完毕，见30-coupon-order-delivery.md。当前初始化45张表，仍缺37张业务表，另2演示表不纳入；不运行真实DDL。

2026-10-28 后续F02批次已将售后迁为标准表/数值主键并接入日志、本人取消与后台处理；当前46张初始化表，仍缺36张业务表。完整源字段和App交互尚未完成，见32-aftersale-log-delivery.md。

2026-10-29 F03批次：收藏、浏览足迹两表及独立各层代码、后台只读查询和小程序操作已接入。收藏按源报重复冲突，足迹重访恢复、上限100、批量隐藏/清空；复用基础锁及事务，详情登录后受保护记录。SKU辅助规格表已合并后当前47张初始化表，仍有34张参考业务表无对应表，另2演示不纳入。后端282、React172、小程序93项测试通过，非真实数据库或端到端验收。具体差异与证据见44-product-interaction-delivery.md。

## 2026-10-29 用户范围调整

用户明确“不需要迁移文章相关代码”。文章及文章分类不再迁移；本轮已撤回对应后端各层、两张SQL表、菜单权限、网关公开路由、后台/小程序页面及接口文档。源清单标 excluded_by_user，不将其算作已迁移或继续补齐的缺口。保留已交付的商品收藏和浏览足迹。当前初始化仍47张表，参考未对应业务表34张，其中文章相关2张明确排除；当前范围尚需迁移32张业务表，另2演示表仍排除。详见45-article-scope-adjustment.md。

2026-10-29 F03评价批次：既有评价表扩展两项评分、图片/匿名、真实会员与原订单商品快照、回复状态及逻辑删除；补公开可见分页、后台管理页及小程序读写。源虚拟评价创建、订单项ID/评价状态联动、额外后台筛选、统计导出及拍照上传仍待迁移，不标全量完成。无新增业务表，初始化仍47张，范围内32张业务表仍无对应表。详见47-product-comment-delivery.md。

2026-10-29 F02/F03订单项评价联动：真实行ID、明细/整单评价状态、全部评价完成时间和33号日志已贯通，支持远程已保存后的原请求修复与旧记录安全关联。自动评价等仍待迁移；仍47张初始化表、范围内32张业务表未对应。详见49-order-item-comment-delivery.md。

2026-10-29 F03后台评价筛选批次：源昵称/订单ID/商品名/创建时间范围筛选及reply_status查询已接入后端与React页面，相关331后端及180前端测试通过。未新增表，剩余业务表数量不变；虚拟创建/自动评价/统计导出/上传仍待迁移。详见51-comment-filter-delivery.md。

2026-10-29评价源差异复核：参考评价控制器/服务/Mapper没有独立统计导出，撤销此前这两项缺口；不影响F11整体统计。真实缺口为后台自评、系统评价、多商品逐项提交及图片上传，见52-comment-source-audit.md。

2026-10-29后台自评已接入独立创建链路与表单，会员0/无订单关联按源语义；仍需系统评价、同页多商品提交及图片上传。无新增表，详见54-comment-self-delivery.md。

2026-10-29系统评价前置：补trade_order.receive_time及收货事务写入，历史NULL不猜测回填。自动评价任务、期限配置及系统保存仍待迁移；见56-order-receive-time-delivery.md。

2026-10-29系统评价已由Spring调度器实现，默认收货7天，独立私有系统保存与34日志，失败原内容重试修复；部署启用及真实网络未验收。见58-system-comment-delivery.md。

2026-10-29小程序同页多商品评价已接入真实行ID、顺序提交、部分结果及未知原请求恢复，108项小程序测试通过；真机未验收，图片上传仍待迁移。见60-order-comment-page-delivery.md。

2026-10-29评价图片选择上传已接入单项/多商品页，复用system文件存储、会员独立路由与网关身份，system133/gateway32/小程序112测试通过；真实OSS、路由发布及真机待验收。见62-comment-upload-delivery.md。

2026-10-29评价图片预览/移除已接入单项及整单页，保持提交附件一致和未知结果冻结；116项小程序测试通过，未真机验收。见64-comment-image-interaction-delivery.md。

2026-10-29 F04会员后台备注字段及独立web契约/表单已接入，App与认证不返回内部备注；同步修正后台会员资料/member-users调用路径。其他会员字段仍待迁移，见66-member-mark-delivery.md。

2026-10-29 F04会员后台姓名/地区编号字段贯通且App/认证不暴露，旧会员更新保留；后台地区名称解析与选择器尚未完成。相关318后端及186前端测试通过，见68-member-identity-delivery.md。

2026-10-29 F04后台地区选择和名称展示已接入，system使用源CSV提供地区树，名称由前端真实路径派生；member服务端areaName及注册登录审计仍待迁移。详见70-member-area-delivery.md。

2026-10-29 F04成功登录时间由member数据库写入，auth会话成功后RPC记录且审计失败尝试撤销，后台可查；相关322后端/190前端测试通过。可信IP及注册来源未迁移，见72-member-login-audit-delivery.md。

2026-10-29 F04登录连接IP已从网关remoteAddress经内部头/auth/RPC到member落库并后台展示；不解析客户端转发头，可信代理链和注册来源仍未完成。详见74-member-login-ip-delivery.md。

2026-10-29 F04后台签到记录昵称筛选/当前昵称展示已接入，独立请求去掉未生效name/status，XML关联及统一分页count；源签到主流程本批不变。见76-sign-record-nickname-delivery.md。

2026-10-30 F04后台积分流水昵称/业务类型组合筛选与当前昵称展示已接入，签到昵称搜索控件修正；会员35/React193项测试通过。源数值业务枚举、积分title和表字段完整对齐仍未完成，见78-point-record-filter-delivery.md。

2026-10-30 F04积分源编号/标题/描述写入快照已接入明确业务映射，原幂等业务码保留；后台及小程序展示，旧值不回填。源积分表及数值筛选契约最终替换仍待迁移，见80-point-record-metadata-delivery.md。

2026-10-30 F04积分源数值类型筛选已接入pointBizType，中文枚举选择及原业务码组合，历史NULL不推断；源独立积分表及最终字段契约仍待迁移。见82-point-numeric-filter-delivery.md。

2026-10-30 F04积分已独立至member_point_record，按表DO/Mapper/Gateway，原资产接口暂作应用兼容路由；经验仍旧表。旧库复制脚本保留ID且不删归档，真实升级未执行；最终字段/审计/经验拆分仍待迁移。见84-point-table-delivery.md。

2026-10-30 F04经验已独立到member_experience_record，源经验元数据及后台/App展示贯通；旧泛化表实现移除，新库不再建旧表，老库归档保留。335后端/196React/118小程序通过；源完整审计/最终HTTP契约和兼容收敛仍待迁移，见86-experience-table-delivery.md。

2026-10-30 F04泛化MemberAssetRecordGateway及Impl已退役，成长编排与查询直接依赖逐表Point/ExperienceGateway；无新路由Service。应用共享投影/最终HTTP契约仍保留，见88-asset-gateway-retirement-delivery.md。

2026-10-30 F04积分/经验DO复用本地BaseDO并补审计列、XML映射；新行取上下文、重试不变，历史不回填。源deleted/最终HTTP字段契约等仍待收敛，见90-ledger-audit-delivery.md。
