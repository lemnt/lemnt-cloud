# 积分配置管理闭环交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接107；源MemberConfigServiceImpl/DO及TradePointUsePriceCalculator/TradePointGiveCalculator。

修正后台积分配置请求为实际/growths/config，保存四项白名单保留false/0；空GET为null，页面明确未配置草稿，用户保存才持久化。替换“交易尚未接入”过期提示，每单抵扣上限正确标为积分而非分；关闭开关同时停用抵扣与按实付赠送，与源实际代码一致。现有正确的mall计算不改，只新增验证。

配置按实际首条ID读写，空表首次insert，存在updateById并核对行数；不依赖固定1。ConfigDO使用Long自增ID及BaseDO审计，Mapper仅BaseMapper，移除注解行锁和重复CRUD。旧库point_config_audit.sql在H2真实升级保留ID77/配置，审计未知NULL，后续更新仍是ID77。新V2保留原默认seed，真实库未执行。

移除ConfigGateway.lockCatalog及等级配置对积分行的依赖；growth域锁适配lemnt-common-lock/LockTemplate/Redisson，不造锁表。同事务取一次，覆盖数据库commit/rollback，获取失败统一409；空表也可串行首次保存，等级可在无积分配置行时正常创建。增加基础锁依赖，Redis连接需随部署配置，本批未连真实Redis。RPC配置缺失明确409，不是NPE。

验证：member/auth/mall/trade完整verify通过后，对新增配置失败回滚用例及import整理重跑member verify通过。最终81/18/119/158共376项，失败/错误/跳过0。member新增4项H2业务测试：无配置首次保存与RPC读回/等级无行依赖、并发首次单行、旧ID77升级/审计保留、注入约束失败回滚后可保存；新增3项锁事务测试提交后独立连接可见才释放、回滚/同事务复用、无事务及获取失败拒绝；新增1项MockMvc四字段路径/单位绑定。mall新增积分5分/上限2积分得0.10元、实付9.90赠29及开关关闭的真实报价测试。未改结算算法。

React211项通过，build/lint/format:check/typecheck通过，构建仅有既有大chunk提示。新增测试真实路径、null不补服务端配置、四字段白名单false/0、未保存草稿及正确单位。小程序123项回归通过。git diff --check通过。证据evidence/point-config-verification.json及/tmp/point-config-*.log；接口docs/member-point-config-api.md，主接口文档过期单位/接入状态已同步修正。

未commit/push，未执行真实MySQL/Redis/Nacos/部署HTTP/浏览器E2E/微信验收。会员域源deleted、业务日期兼容及最终路径等仍有差异；不宣称全量迁移完成。文章排除。
