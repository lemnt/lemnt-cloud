# 完整规格快照列表贯通交付

2026-10-28，承接37设计，继续使用code-software-engineer。参考本地芋道TradeOrderItemDO/AfterSaleDO的Property列表以及当前SKU字典关联。完成本批纵向链路，不代表全部商城迁移完成。

## 已实现

- ProductSnapshot读取SKU规格行时同时取propertyId/propertyName/valueId/valueName，按保存的名称排序；不查当前字典补旧编号。旧未关联规格保留名称和值、两个编号为空；整组未知null与已知无规格[]区分。
- properties从名值映射调整为独立嵌套Property列表，贯通mall目录客户端、领域/应用、app/web结算响应、促销计价/会员折扣/优惠券/积分及运费分摊、trade跨服务DTO、订单项/可退额度/售后申请及各层独立响应。各层Converter显式映射，不用领域对象直接作HTTP或RPC数据对象。
- 订单项与售后继续使用现有properties JSON列，新写列表。基础设施读取旧JSON对象时仅还原名值、不猜测ID、不反查商品或字典。售后从原订单项复制完整列表，审批/退款不改变原规格。
- 报价指纹包含规格四字段并稳定排序，编号变化但名称和值未变时旧报价也拒绝。原订单号换商品快照重放仍拒绝。
- Redis预览v3→v4，使未提交旧预览重新确认；提交索引/恢复上下文v2保留，读取旧submission只转换order.items.properties，不改写订单号、请求编号、原指纹或Redis索引。真实序列化测试覆盖恢复。
- RPC验证规格编号成对且为正数、名称和值长度、重复规格项和数量上限；服务端商品快照不由HTTP请求覆盖。成功R/分页rows/total及统一异常处理不变。
- React结算/订单/售后解码和展示规格列表，长ID按字符串保存；兼容旧名值对象读取，拒绝部分缺号及失真数字。商品管理的properties/specValueIds编辑合同保持36批次定义，未误改为已全面同构。
- 接口文档原位更新，并补充版本与部署边界。本次没有新增表/列或升级DDL，没有实际数据库升级、提交、推送或部署。

## 验证

- 后端member/mall/trade及依赖：274项测试，失败/错误/跳过均0。clean verify日志`/tmp/property-list-clean-verify.log`，最终修改后verify日志`/tmp/property-list-final-verify.log`。主代码编译、全部测试编译及打包通过。
- 双库H2 MySQL模式、生产初始化DDL、真实MyBatis-Plus Gateway和事务代理：通过商品应用服务选择真实字典编号后下单，落库订单及售后保留原编号/名称；随后修改目录名、规格值和图片不改历史；仅规格编号变化拒绝旧报价且无订单写入。既有优惠分摊、积分、运费、支付和售后用例全部回归。
- 生产Redis序列化器往返保存完整规格列表；旧v2提交回执保留原订单号/请求编号/指纹，只转换已有规格名称和值且不写Redis。旧数据库名值JSON、空对象及NULL分别转换为缺号列表、[]和null。
- 实际HTTP Jackson配置验证四字段列表及Long最大值无精度损失；各请求/响应隔离，现有接口测试通过。
- React170项测试通过，日志`/tmp/property-list-ui-tests.log`；类型检查及生产构建、ESLint、Prettier通过，日志`/tmp/property-list-ui-build.log`、`/tmp/property-list-ui-lint.log`、`/tmp/property-list-ui-format-check.log`。
- 小程序81项测试通过，日志`/tmp/property-list-mini-tests.log`，仅验证额外响应字段兼容，不代表已迁移完整小程序页面。
- git diff --check通过。工作区期间存在外部提交更新，保留全部已有修改，本代理未执行Git提交或推送。

## 剩余差异与交接

本批仅将结算、订单和售后的properties改为源项目四字段列表。商品管理写入仍是名称映射加specValueIds映射，源SPU/SKU完整字段/规格模式、订单其他金额与状态、售后资料、App流程及业务缺表仍按完整清单推进。初始化仍46张表，36张业务缺表未因本批减少，支付demo排除；真实渠道SDK按用户要求后置。

部署时需要协调mall/trade及使用其DTO的消费者，不能让只支持旧对象格式的服务读取新列表；后台已兼容旧对象展示。旧库前序基础列和字典关联升级仍按实际版本执行，本次没有额外DDL。真实MySQL/Redis/Dubbo/MQ、部署HTTP、浏览器/真机端到端均NOT RUN，不能用隔离测试代替实际闭环验收。QA重点验证跨服务滚动升级、旧提交重试、无规格/旧未知规格展示、商品修改后查看原订单及售后。
