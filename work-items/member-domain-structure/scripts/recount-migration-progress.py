from pathlib import Path
from collections import Counter
import json,hashlib
ROOT=Path(__file__).resolve().parents[3]
REF=Path('/Users/wangxx/demo/yudao-cloud')
# B=业务与接口，D=数据/事务，W=后台交互，A=App交互；null表示源无独立交互/持久化需求。
rows=[]
def add(id,group,name,scores,source,evidence,gap):
 """登记人工复核分值；脚本只重算及校验证据，不自动判定业务等价。"""
 rows.append(dict(id=id,group=group,name=name,scores=dict(zip(['backend','data','admin','app'],scores)),sourceControllers=source.split(','),evidenceSelectors=evidence.split('|') if evidence else [],gap=gap))
add('M01','会员','后台会员资料及筛选',[.5,.5,1,None],'MemberUserController','MemberUserService.java|MemberUserController.java|MemberUserPage.tsx|MemberAccountIntegrationTest.java','注册IP/终端及部分源资料契约未对齐；已有电话、标签、等级分组及时间筛选不能算未迁移')
add('M02','会员','App资料与密码维护',[.5,.5,None,.5],'AppMemberUserController','AppMemberUserController.java|AppMemberPasswordController.java|pages/member/index.js|services/member.js','已有本人资料及密码维护；头像交互、源剩余资料字段未完整对应；短信微信排除')
add('M03','会员','认证会话刷新退出及停用撤销',[1,1,None,1],'AppAuthController','MemberAuthenticationService.java|MemberTokenGatewayImpl.java|TokenService.java|pages/member-login/index.js|MemberAuthenticationIntegrationTest.java','约定范围内已实现；短信微信认证不计入；真实Redis/RPC验收另列')
add('M04','会员','收货地址与默认地址',[1,1,1,.5],'AppAddressController,AddressController','MemberAddressService.java|MemberAddressAreaGatewayImpl.java|pages/member-addresses/index.js|MemberUserPage.tsx|MemberAccountIntegrationTest.java','地址归属/审计/地区名称已补齐；App仍输入地区编号，未对齐源地区选择交互')
add('M05','会员','会员分组管理',[.5,1,1,None],'MemberGroupController','MemberGroupController.java|MemberUserService.java|MemberMetadataPage.tsx|MemberAccountIntegrationTest.java','CRUD/分页/分配已接入，名称唯一等源差异仍需核对')
add('M06','会员','会员标签管理与绑定',[1,1,1,None],'MemberTagController','MemberTagController.java|MemberUserService.java|MemberMetadataPage.tsx|MemberAccountIntegrationTest.java','本单元核心增改删查、归属绑定、分页与引用保护已有代码及用例')
add('M07','会员','等级配置与启用列表',[1,1,1,1],'MemberLevelController,AppMemberLevelController','MemberGrowthService.java|MemberLevelController.java|MemberMetadataPage.tsx|pages/member-benefits/index.js|MemberAccountIntegrationTest.java','配置/列表/筛选/审计/引用删除保护已覆盖；等级变更单列')
add('M08','会员','手工等级调整及等级记录',[1,1,1,None],'MemberLevelRecordController,MemberUserController','MemberGrowthService.java|MemberUserLevelController.java|MemberRecordPage.tsx|MemberAccountIntegrationTest.java','当前等级重复调整、取消、快照、筛选和重放已覆盖；真实环境未验收')
add('M09','会员','积分配置调整与流水',[.5,1,1,1],'MemberConfigController,MemberPointRecordController,AppMemberPointRecordController','MemberGrowthService.java|MemberPointConfigController.java|MemberRecordPage.tsx|MemberConfigPage.tsx|pages/member-benefits/index.js','配置/过滤/流水已实现；源零调整无操作等细节尚未全部收敛，不算满分')
add('M10','会员','经验流水与自动等级计算',[1,1,1,1],'MemberExperienceRecordController,AppMemberExperienceRecordController','MemberGrowthService.java|MemberExperienceRecordController.java|MemberRecordPage.tsx|pages/member-benefits/index.js|MemberAccountIntegrationTest.java','负数归零、源流水值、等级匹配/不匹配及降级等核心场景已覆盖')
add('M11','会员','签到配置签到与记录',[1,1,1,1],'MemberSignInConfigController,AppMemberSignInRecordController','MemberGrowthService.java|MemberSignConfigController.java|MemberSignRecordController.java|MemberConfigPage.tsx|pages/member-benefits/index.js','独立ID配置、天数唯一、签到重复冲突、奖励流水关联、查询和页面已覆盖')
add('P01','商品','商品分类',[.5,.5,.5,1],'ProductCategoryController,AppCategoryController','CategoryController.java|ProductCatalogService.java|CategoryPage.tsx|pages/store/index.js|CatalogPersistenceTest.java','基础分类/树/店铺已实现，源层级与经营字段、完整管理交互仍待核对')
add('P02','商品','商品品牌',[.5,.5,.5,None],'ProductBrandController','BrandController.java|ProductCatalogService.java|BrandPage.tsx|CatalogPersistenceTest.java','基础CRUD已有，源完整排序/状态等字段及页面未完全对应')
add('P03','商品','规格及规格值字典',[1,1,.5,None],'ProductPropertyController,ProductPropertyValueController','ProductSpecController.java|ProductSpecValueController.java|ProductSpecService.java|SkuSpecFields.tsx','后端独立字典及SKU引用已有；后台仅选择引用，缺完整独立字典管理页面')
add('P04','商品','SPU/SKU及商品经营信息',[.5,.5,.5,.5],'ProductSpuController,AppProductSpuController','ProductManagementService.java|ProductSpuController.java|SpuPage.tsx|SpuEditor.tsx|pages/product/index.js','已有真实SKU/规格JSON和订单快照；源完整经营字段、导出及详情交互仍存在差异')
add('P05','商品','库存管理与预占确认释放',[1,1,.5,None],'ProductSpuController','ProductInventoryService.java|ProductManagementService.java|SpuPage.tsx|CheckoutFlowIntegrationTest.java','后端预占/确认/释放与台账已有；后台库存经营交互未完整对照')
add('P06','商品','商品评价管理与会员评价',[1,1,1,1],'ProductCommentController,AppProductCommentController','ProductCommentService.java|CommentController.java|CommentPage.tsx|pages/comments/index.js|pages/order-comment/index.js|CommentFlowPersistenceTest.java','真实订单项、多项评价、自评、评分图片匿名、回复显隐、自动评价已覆盖约定业务；真实OSS另验')
add('P07','商品','商品收藏',[1,1,.5,1],'ProductFavoriteController,AppFavoriteController','ProductFavoriteService.java|ProductInteractionPanels.tsx|pages/favorites/index.js|ProductInteractionPersistenceTest.java','后端和App已有，后台为商品内面板，未完整对齐源独立列表交互')
add('P08','商品','浏览足迹',[1,1,.5,1],'ProductBrowseHistoryController,AppProductBrowseHistoryController','ProductBrowseHistoryService.java|ProductInteractionPanels.tsx|pages/browse-history/index.js|ProductInteractionPersistenceTest.java','重访/裁剪/隐藏/清空已有，后台面板与源完整分页交互仍有差异')
add('C01','购物结算优惠券','购物车',[.5,1,None,.5],'AppCartController','CartService.java|AppCartController.java|pages/index/index.js|CheckoutFlowIntegrationTest.java','已有列表/设置数量选中/单删，缺源完整批量选中、批删、规格重置和数量汇总')
add('C02','购物结算优惠券','结算计价与Redis结算单',[.5,1,None,.5],'AppTradeOrderController','SettlementPreparationService.java|SettlementService.java|ActivityPricingService.java|pages/index/index.js|CheckoutFlowIntegrationTest.java','促销/等级/积分/券/运费已接计价；完整立即购买与源结算交互/契约仍未全部对齐')
add('C03','购物结算优惠券','优惠券模板管理',[.5,1,.5,None],'CouponTemplateController','CouponService.java|CouponTemplateController.java|CouponTemplatePage.tsx|CouponSnapshotPersistenceTest.java','模板快照/范围/有效期等已有，源全部配置与表单操作需逐项收口')
add('C04','购物结算优惠券','后台发券回收及持券管理',[.5,1,.5,None],'CouponController','CouponIssuanceService.java|CouponController.java|CouponRecordPage.tsx|CouponRecordsPanel.tsx','持券分页/发放等已有；源完整批量发券回收和查询表单仍需补齐')
add('C05','购物结算优惠券','App领券中心与本人券包',[.5,1,None,.5],'AppCouponTemplateController,AppCouponController','AppCouponCenterController.java|AppCouponController.java|pages/coupons/index.js','中心/领取/券包已有，单券详情、数量及源完整筛选交互未全')
add('C06','购物结算优惠券','下单锁券支付核销关单释放',[1,1,None,None],'AppTradeOrderController','SettlementRecordService.java|SettlementLifecycleClientImpl.java|CheckoutFlowIntegrationTest.java|CouponSnapshotPersistenceTest.java','约定同步锁券/核销/释放及持券快照有生产调用和测试；真实RPC另验')
add('A01','促销运营','满减送活动',[.5,.5,0,0],'RewardActivityController,AppRewardActivityController','ActivityManagementService.java|ActivityPricingService.java|RewardActivityController.java|ActivityAlignmentPersistenceTest.java','满减管理/结算已有；按件数、赠券赠积分包邮等源完整规则及后台/App页面未全')
add('A02','促销运营','限时折扣活动',[.5,1,0,None],'DiscountActivityController','DiscountActivityController.java|ActivityManagementService.java|ActivityPricingService.java|ActivityAlignmentPersistenceTest.java','SKU减价/折扣和结算已接；源完整约束仍待核对，缺后台管理页')
add('A03','促销运营','App商品活动聚合展示',[0,None,None,0],'AppActivityController','','没有对应活动聚合接口及App活动入口')
for id,name,src in [('A04','秒杀及秒杀时段','SeckillActivityController,SeckillConfigController,AppSeckillActivityController'),('A05','拼团活动及参团记录','CombinationActivityController,CombinationRecordController,AppCombinationRecordController'),('A06','砍价活动助力及记录','BargainActivityController,BargainRecordController,AppBargainHelpController'),('A07','积分商城活动','PointActivityController,AppPointActivityController'),('A08','分销关系及推广用户','BrokerageUserController,AppBrokerageUserController'),('A09','佣金冻结解冻与退款回退','BrokerageRecordController,AppBrokerageRecordController'),('A10','分销提现','BrokerageWithdrawController,AppBrokerageWithdrawController'),('A11','Banner轮播','BannerController,AppBannerController'),('A12','装修模板及页面','DiyPageController,DiyTemplateController,AppDiyPageController'),('A13','客服会话及消息','KeFuConversationController,KeFuMessageController,AppKeFuMessageController')]:
 add(id,'促销运营',name,[0,0,0,0],src,'','未发现对应目标业务域/表/路由和页面，未迁移')
add('T01','订单售后物流','后台订单管理及操作日志',[.5,.5,.5,None],'TradeOrderController','OrderManagementController.java|FulfillmentOrderController.java|OrderFulfillmentPage.tsx|OrderLogPanel.tsx','分页详情/备注/发货/日志已有；完整调价、改址及源筛选/字段未全')
add('T02','订单售后物流','App订单中心',[.5,1,None,0],'AppTradeOrderController','com/lemnt/trade/adapter/app/order/AppOrderController.java|pages/index/index.js','App仅取消/收货，缺本人分页、分类数量、详情/删除/物流及独立订单中心页面')
add('T03','订单售后物流','同步提交与订单落库',[1,1,None,.5],'AppTradeOrderController','OrderPlacementService.java|SettlementPreparationService.java|CheckoutFlowIntegrationTest.java|pages/index/index.js','同步库存预占/锁券/订单写入已有；源完整下单表单和入口仍未全')
add('T04','订单售后物流','超时关单及补偿',[1,1,None,None],'TradeOrderController','TradeOrderScheduler.java|OrderCloseService.java|OrderCloseServiceTest.java|CheckoutFlowIntegrationTest.java','超时关闭、状态竞争/恢复及统一库存券释放有代码和测试，真实渠道查询后置')
add('T05','订单售后物流','自动收货及自动评价',[.5,1,None,None],'TradeOrderController','TradeOrderAutoCommentScheduler.java|AutoCommentSchedulerTest.java','自动评价按调度器已有；自动收货尚未发现对应任务')
add('T06','订单售后物流','后台售后退款退货及日志',[.5,1,.5,None],'AfterSaleController','AfterSaleController.java|AfterSaleLogController.java|RefundRequestPage.tsx|AfterSaleDetailPanel.tsx','部分退款/退货退款/审批/收货日志已有；源全部字段筛选和交互未完全对齐')
add('T07','订单售后物流','App售后申请及售后中心',[.5,1,None,0],'AppAfterSaleController,AppAfterSaleLogController','AppAfterSaleController.java','目前只有本人取消；申请/分页/详情/退货物流/日志及App售后页未全')
add('T08','订单售后物流','售后逆向退款交易',[1,1,.5,None],'PayRefundController','PaymentRefundService.java|PaymentRefundController.java|RefundRequestPage.tsx|PaymentRefundChannelGatewayTest.java','退款建单/额度/状态/回调已有，后台仅售后发起及查看子集，缺完整支付退单页；渠道SDK排除')
add('T09','订单售后物流','退款库存券积分经验回退',[.5,.5,None,None],'TradeOrderController','RefundBenefitsService.java|OrderBenefitsGatewayImpl.java|SettlementLifecycleClientImpl.java|MemberGrowthClientImpl.java','生产回调链已存在，旧未接通结论撤销；源所有部分退款比例/规则和失败恢复仍待逐项对齐')
add('T10','订单售后物流','发货物流单及轨迹查询',[.5,1,.5,0],'TradeOrderController','ShipmentController.java|ShipmentQueryController.java|OrderFulfillmentPage.tsx','后台发货和物流单接口已有，缺完整独立物流页面及App物流查询；真实轨迹渠道排除')
add('T11','订单售后物流','快递公司管理及选择',[1,1,0,0],'DeliveryExpressController,AppDeliverExpressController','DeliveryExpressController.java|DeliveryManagementService.java|DeliveryPersistenceTest.java','后端字典已有；未注册独立后台页，App退货快递选择链路未全')
add('T12','订单售后物流','运费模板及结算运费',[1,1,0,None],'DeliveryExpressTemplateController','DeliveryExpressTemplateController.java|DeliveryManagementService.java|DeliveryPriceService.java|SettlementDeliveryService.java|DeliveryPersistenceTest.java','模板/按地区件重体积/包邮计价已接；后台只有商品模板选择，缺完整模板管理页')
add('T13','订单售后物流','自提门店及店员核销',[0,0,0,0],'DeliveryPickUpStoreController,AppDeliverPickUpStoreController','','无对应门店/店员/核销流程及页面')
add('T14','订单售后物流','交易配置',[.5,.5,0,0],'TradeConfigController,AppTradeConfigController','TradeConfigController.java|TradeConfigService.java','配置后端部分已有，源售后原因/配送自提等完整配置和管理/App接入未全')
add('Y01','支付业务','支付单管理和App支付状态',[.5,.5,0,.5],'PayOrderController,AppPayOrderController','PaymentController.java|AppPaymentController.java|pages/index/index.js|PaymentNotificationServiceTest.java','本地支付单/状态已有，源完整支付尝试/字段/后台支付单页与App收银台未全；SDK不计欠账')
add('Y02','支付业务','支付应用管理',[0,0,0,None],'PayAppController','','无PayApp对应完整业务/表和后台页')
add('Y03','支付业务','支付渠道配置及查询',[.5,0,0,0],'PayChannelController,AppPayChannelController','PaymentChannelController.java','仅支持渠道清单接口，缺源渠道配置存储和后台/App交互；实际SDK排除')
add('Y04','支付业务','通知任务日志及重试管理',[.5,0,0,None],'PayNotifyController','PaymentNotificationService.java|PaymentNotificationServiceTest.java','支付通知业务处理已有，但无源可靠通知任务表/日志/调度及后台管理')
for id,name,src in [('Y05','钱包账户冻结及流水','PayWalletController,PayWalletTransactionController,AppPayWalletController'),('Y06','充值套餐充值及充值退款','PayWalletRechargeController,PayWalletRechargePackageController,AppPayWalletRechargeController'),('Y07','转账业务','PayTransferController,AppPayTransferController')]:
 add(id,'支付业务',name,[0,0,0,0],src,'','无对应支付扩展业务域/表/后台/App页面')
for id,name,src in [('S01','商品经营统计','ProductStatisticsController'),('S02','交易经营统计','TradeStatisticsController'),('S03','会员经营统计','MemberStatisticsController'),('S04','支付经营统计','PayStatisticsController')]:
 add(id,'统计',name,[.5 if id=='S02' else 0,0,0,None],src,'OrderManagementController.java' if id=='S02' else '','交易仅现有简要订单汇总，不能替代源趋势/排行/日统计及统计页' if id=='S02' else '未迁移对应源统计服务/任务及经营分析页')
# Explicitly assign remaining source controllers to their same business capability.
source_extra={'M11':['AppMemberSignInConfigController','MemberSignInRecordController'],
 'A04':['AppSeckillConfigController'],'A05':['AppCombinationActivityController'],
 'A06':['AppBargainRecordController','AppBargainActivityController','BargainHelpController'],
 'A12':['AppDiyTemplateController'],'Y05':['AppPayWalletTransactionController'],
 'Y06':['AppPayWalletRechargePackageController']}
for row in rows:
 row['sourceControllers']+=source_extra.get(row['id'],[])
# User confirmed: frontend service prefix; gateway forwarding depends on each service's real path.
for row in rows:
 if row['id'] in ['M01','M04','M05','M06','M07','M08','M09','M11']:
  row['scores']['admin']=.5
  row['gap']+='；后台memberApi中本功能部分调用仍无服务前缀，不满足本轮确认的前缀规范，待网关转发联调'
# Cross-check real caller paths, not just isolated mocked tests.
for row in rows:
 if row['id'] in ['M07','M09','M10','M11']:
  row['scores']['app']=.5
 if row['id'] in ['M02','M07','M09','M10','M11']:
  row['gap']+='；去服务前缀后member.js业务路径与App Controller仍不一致，实际Nacos未核验，不能计App链路完成'
# Resolve current files, not old manifests. Exact paths and hashes make the review reproducible.
roots=[ROOT/'lemnt-server', ROOT/'lemnt-common',ROOT/'lemnt-ui/src',ROOT/'lemnt-miniapp/pages',ROOT/'lemnt-miniapp/services',ROOT/'lemnt-miniapp/tests']
files=[p for r in roots for p in r.rglob('*') if p.is_file() and not any(x in p.parts for x in ['target','node_modules','.git'])]
source_files=list((REF/'yudao-module-member').rglob('*Controller.java'))+list((REF/'yudao-module-mall').rglob('*Controller.java'))+list((REF/'yudao-module-pay').rglob('*Controller.java'))
missing=[]
for row in rows:
 source_names=row.pop('sourceControllers')
 row['sourceFiles']=sorted(str(p.relative_to(REF)) for p in source_files if p.stem in source_names)
 assert row['sourceFiles'],row['id']
 ev=[]
 for selector in row.pop('evidenceSelectors'):
  hits=[p for p in files if str(p).endswith('/'+selector)]
  if not hits: missing.append((row['id'],selector))
  ev.extend(str(p.relative_to(ROOT)) for p in hits)
 row['evidenceFiles']=sorted(set(ev))
 assert not any(v and not row['evidenceFiles'] for v in row['scores'].values()),row['id']
 vals=[v for v in row['scores'].values() if v is not None]
 row['points']=sum(vals);row['applicableChecks']=len(vals);row['percent']=round(100*sum(vals)/len(vals),1)
 row['state']='checks_met' if all(v==1 for v in vals) else ('partial' if any(v>0 for v in vals) else 'not_implemented')
assert len(rows)==63
assert not missing,missing
in_scope=[str(p.relative_to(REF)) for p in source_files if not any(t in str(p.relative_to(REF)) for t in ['/article/','/demo/','/social/'])]
assigned={x for r in rows for x in r['sourceFiles']}
assert not set(in_scope)-assigned,sorted(set(in_scope)-assigned)
print('ROWS',len(rows),'missing selectors',missing,'source controllers assigned',len(assigned))
summary={}
for group in dict.fromkeys(r['group'] for r in rows):
 rs=[r for r in rows if r['group']==group];pts=sum(r['points'] for r in rs);n=sum(r['applicableChecks'] for r in rs)
 summary[group]={'units':len(rs),'points':pts,'applicableChecks':n,'percent':round(100*pts/n,1),'states':dict(Counter(r['state'] for r in rs))}
dimensions={}
for k in ['backend','data','admin','app']:
 vals=[r['scores'][k] for r in rows if r['scores'][k] is not None]
 dimensions[k]={'points':sum(vals),'applicableChecks':len(vals),'percent':round(100*sum(vals)/len(vals),1)}
pts=sum(r['points'] for r in rows);checks=sum(r['applicableChecks'] for r in rows)
result={'date':'2026-10-31','scope':'Latest full-migration F01-F12 code scope; approved six-layer/domain/REST/framework adaptations respected','exclusions':['文章与文章分类','会员短信/微信认证','支付退款物流真实渠道SDK','支付演示业务及演示表'],'method':{'unit':'63 named business capabilities, equal applicable checkpoint weights','checkpoints':['backend','data','admin','app'],'scores':{'1':'本检查项核心链路已存在并有可定位实现/相关验证，未见阻断该项的已知核心缺口；不等于逐字段完全等价或部署验收','0.5':'已有对应实现，但源业务分支/字段/操作/交互缺失或本轮未完成完整等价核对，保守记部分','0':'未发现对应实现或对应检查项缺失','null':'源功能没有独立该层交互/存储需求，不计入分母'},'overallFormula':'sum(checkpoint scores) / count(applicable checkpoints) * 100','stateMeaning':'checks_met only means all code-stage checks in this scoped capability meet rubric, not end-to-end accepted','limitations':['静态代码/调用链及既有测试证据复核；本次没有执行全套构建或真实端到端测试','不是工时、代码行数、源文件复制率；0.5为明确的阶段分值，不代表该项实际剩余工时恰为50%','按同一功能合并多个源Controller，避免按文件数量夸大或缩小进度','源码尚有字段/契约差异，因此只能称代码阶段迁移进度，不能称完整复刻验收完成率']},'summary':{'units':len(rows),'points':pts,'applicableChecks':checks,'percent':round(100*pts/checks,1),'states':dict(Counter(r['state'] for r in rows)),'groups':summary,'dimensions':dimensions},'units':rows,'unresolvedEvidenceSelectors':missing,'sourceControllerCoverage':{'inScope':len(in_scope),'assigned':len(set(in_scope)&assigned),'unassigned':[]},'sourceRoot':str(REF),'workspace':str(ROOT)}
module_rows={'member':[],'mall':[],'trade':[]}
for row in rows:
 module='member' if row['id'].startswith('M') or row['id']=='S03' else ('mall' if row['id'][0] in ['P','C','A'] or row['id']=='S01' else 'trade')
 row['module']=module
 module_rows[module].append(row)
result['summary']['modules']={}
for name,rs in module_rows.items():
 points=sum(r['points'] for r in rs);checks=sum(r['applicableChecks'] for r in rs)
 result['summary']['modules'][name]={'units':len(rs),'points':points,'applicableChecks':checks,'percent':round(100*points/checks,1)}
result['moduleAllocation']='认证按会员功能计入member但代码仍在auth；购物车/结算/促销归mall；支付/钱包归trade；会员统计归member、商品统计归mall、交易/支付统计归trade'
result['frontendPrefixRule']='前端保留服务前缀；网关按各服务Controller实际路径处理转发，不能全局盲目StripPrefix；gateway禁止Dubbo'
result['contractFindings']=[
 {'caller':'/member/app/user','afterRemovingServicePrefix':'/app/user','controller':'/app/users/user','status':'business_path_mismatch'},
 {'caller':'/member/app/user/password','afterRemovingServicePrefix':'/app/user/password','controller':'/app/users/user/password','status':'business_path_mismatch'},
 {'caller':'/member/app/levels','afterRemovingServicePrefix':'/app/levels','controller':'/app/growths/levels','status':'business_path_mismatch'},
 {'caller':'/member/app/signin/config','afterRemovingServicePrefix':'/app/signin/config','controller':'/app/growths/signin/config','status':'business_path_mismatch'},
 {'caller':'/member/app/point-records','afterRemovingServicePrefix':'/app/point-records','controller':'/app/growths/point-records','status':'business_path_mismatch'},
 {'caller':'/member/app/addresses','afterRemovingServicePrefix':'/app/addresses','controller':'/app/addresses','status':'prefix_forwarding_needed_not_a_business_path_mismatch'},
 {'caller':'memberApi /member-users,/groups,/tags,/growth/levels,/growths/config','status':'missing_service_prefix_under_newly_confirmed_rule'}]
result['contractFindingBoundary']='仅仓库静态核对；未读取运行中Nacos配置，不能断言已部署路由一定失败；额外RewritePath可改变结果，需要联调核验'
result['evidenceHashes']={f:hashlib.sha256((ROOT/f).read_bytes()).hexdigest() for f in sorted({f for r in rows for f in r['evidenceFiles']})}
result['referenceHashes']={f:hashlib.sha256((REF/f).read_bytes()).hexdigest() for f in sorted({f for r in rows for f in r['sourceFiles']})}
path=ROOT/'work-items/member-domain-structure/evidence/migration-progress-20261031.json'
path.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(result['summary'],ensure_ascii=False,indent=2))
