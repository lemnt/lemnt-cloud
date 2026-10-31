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

2026-10-30 F04等级记录补变更时等级/折扣及源记录经验值，旧余额列保留并单独映射；手工差值与自动门槛按源路径区分，历史未知NULL。340后端/197前端测试通过，自动无匹配等级状态机差异仍待迁移，见92-level-record-snapshot-delivery.md。

2026-10-30 F04自动无匹配等级保留规则已对齐，匹配低等级仍降级，经验超额扣减余额归零但保留请求流水。344项后端verify通过；源零变更、退款入口等差异尚存，见94-level-transition-delivery.md。

2026-10-30 F04经验流水后台接入源业务编号/类型/标题/时间筛选及React控件，移除经验Req无效name/status。348项后端与199项前端测试通过，接口/统一分页未改；真实HTTP及泛化投影等未完成，见96-experience-record-filter-delivery.md。

2026-10-30 F04等级记录接入等级编号及时间筛选，去掉无效name/status；单表Lambda共用条件，重复CRUD直接BaseMapper。352后端/201前端测试通过，真实部署HTTP和会员域其他差异未完成，见98-level-record-filter-delivery.md。

2026-10-30 F04积分后台标题包含查询、App收入/支出及时间查询和小程序控件接通。356后端/202前端/122小程序测试通过；真实HTTP及微信端未验收，兼容契约等差异未完成，见100-point-record-filter-delivery.md。

2026-10-30 F04签到记录统一源表/字段并补真实时间、基础审计及天数/时间筛选，旧未知时间保持NULL；React/小程序展示贯通。360后端/204前端/123小程序测试通过，真实库升级未执行，源签到规则等差异仍在，见102-sign-record-time-delivery.md。

2026-10-30 F04重复签到改源拒绝409，新奖励流水关联实际签到记录ID，汇总排除未来连续记录；周期/停用/断签回归补齐。363后端verify通过，配置ID CRUD及其他差异待迁移，见104-sign-flow-delivery.md。

2026-10-30 F04签到配置独立ID CRUD、天数唯一冲突、BaseMapper及React权限分离已迁移；旧表升级仅H2验证。367后端/208前端/123小程序回归通过，真实库/路由/授权未执行，见106-sign-config-id-delivery.md。

2026-10-30 F04积分配置实际路径、首次保存/真实首条ID、BaseDO审计及基础事务锁已接通；等级不再借积分配置行加锁，单位提示与源结算一致。376后端/211前端/123小程序测试通过，真实DB/Redis部署未验收，见108-point-config-delivery.md。

2026-10-30 F04等级名称/状态管理筛选、BaseMapper和基础审计及删除错误收敛；复杂删除XML保留保护，元数据三类前端路径修复。380后端/213前端/123小程序通过，真实升级部署未执行，见110-level-management-delivery.md。

2026-10-30 F04分组/标签独立分页、时间审计、Req域归位及Mapper规范收敛，修正标签根路由冲突并接前端分页。384后端/216前端/123小程序通过，真实升级部署未执行，源分组唯一等待核对，见112-group-tag-management-delivery.md。

2026-10-30 F04会员列表源手机号/邮箱/昵称/时间/等级/分组及标签OR筛选贯通，独立前端入参与数组序列化验证。388后端/218前端/123小程序通过，无DDL，部署路由未应用，见114-member-user-filter-delivery.md。

2026-10-30 F04后台会员分页/详情批量返显等级分组名及标签，App契约不变，原历史快照不改。393后端/221前端/123小程序通过，无DDL，部署E2E未验收，见116-member-user-relations-delivery.md。

2026-10-31 F04后台独立资料命令补手机号维护及手机/邮箱占用校验，App字段隔离，资料保存不覆盖资产和关联。398后端/222前端/123小程序通过，无DDL，源Token撤销及完整用户契约待收敛，见118-member-user-update-delivery.md。

2026-10-31 F04后台停用经auth同步撤销会员会话、刷新防复活，复用公共key且无token集合索引，gateway保持无Dubbo。402业务测试、34网关、29公共安全测试通过；真实Redis/RPC部署未验收，见120-member-disable-session-delivery.md。

2026-10-31 F04手工目标等级未变化时按源无操作，保留经验且不制造经验/等级流水，并发验证通过。405业务/34网关/29公共安全测试通过，修复既有新增Redis测试MockMaker不稳定问题，无HTTP/DDL变化，见122-member-level-noop-delivery.md。

2026-10-31 F04地址按源移除20条限制、归属写错误/默认切换审计收敛，user_id与BaseDO升级脚本及BaseMapper逐表存储接通。410业务/34网关/29公共安全/123小程序测试通过；真实库升级未执行，见124-member-address-alignment-delivery.md。

2026-10-31 F04地址列表/详情/默认及后台批量返显地区全称，复用system源资源，前端/小程序结算选址贯通。414业务/system136/gateway34/common-security29、React223、小程序124通过；RPC部署待验，无DDL，见126-member-address-area-name-delivery.md。

2026-10-31进度重新核对：以63个业务单元、214个适用交付检查项替代源文件登记比例；当前代码阶段90.5/214=42.3%，4项本轮检查满分、40项部分、19项未实现。此为阶段估算，非逐字段完全等价或部署验收完成率；前端服务前缀由网关按实际路径转发的确认已纳入口径。详见127-migration-progress-recount.md及证据JSON。

2026-10-31 128整链路批次：统一前端前缀、member业务路径和按服务转发，会员上传走system且保持身份隔离。35网关/223前端/125小程序测试通过，无Dubbo/DDL/部署；127路径缺口在代码合同层关闭，实际Nacos联调仍待验。

2026-10-31 129整业务批次：满减/折扣后台列表详情、增改关闭删除、SPU-SKU及规则选择、权限和幂等菜单种子一起交付；232前端、10项后端聚焦测试通过。赠券/赠积分等源剩余规则和真实浏览器联调未完成。

129同批继续补齐支付单/支付退单/支持渠道三个入口，本批共五页；最终237前端、15项后端聚焦测试通过，两个新增菜单种子均未执行真实库。详见129-backoffice-management-pages-delivery.md。

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
