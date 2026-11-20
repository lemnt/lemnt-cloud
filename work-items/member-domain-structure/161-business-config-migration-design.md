# 第161批：业务配置迁入sys_config设计

2026-11-02，v1，责任Skill：code-software-engineer；需求member-domain-structure。
输入：本轮配置表分析及用户“迁移”指令，基线a457e0e7，保留第160批未提交修改。
参考已读取的工程Skill `references/development-design.md`、`references/java.md`，按本次存储迁移裁剪。

## 范围和合同

- 仅迁移member_config的4个积分字段及trade_config的15个业务字段。签到、秒杀时段、支付渠道等结构化资源表保留。
- sys_config增加两个保留key：`member.point.config`、`trade.config`；每key一个完整JSON对象，单行原子保存及读取，避免多key新旧混读。贸易配置id=1作为既有对外合同保留，金额单位、字段和页面不变。
- 扩大config_value为LONGTEXT并为config_key增加唯一索引。通用配置入口禁止新增、修改、重命名或删除这两个key，防止绕过member/trade现有业务校验。
- 通过RemoteConfigService新增内部完整配置保存方法，GatewayImpl封装Dubbo，system应用层校验保留key及JSON对象后由system数据库单次upsert提交。操作者随调用写入审计；不引入分布式事务，不把本地行锁伪装成远程数据库锁。
- 读取沿用system数据库权威读取，不新增客户端缓存；保存清除该key旧缓存。缺失配置保持显式缺失，RPC失败直接失败，不静默使用旧表或默认开关。
- 保留既有member/trade业务接口、权限、校验和页面。去掉原本只保护这两张表的无效本地事务/交易配置锁调用；会员共享成长配置锁仍供签到、等级等原有业务使用。配送维护原来复用trade_config行锁，迁移为独立DeliveryManagementLockGateway，沿用项目LockTemplate和事务完成释放机制，保持快递编码/模板名称并发保护。

## 任务与验收

1. system-client：保留键、内部保存BO和Dubbo契约；system应用/基础设施：原子保存、通用入口防绕过、唯一约束及审计。
2. member/trade基础设施：替换配置Gateway为RPC/JSON适配，删除因此孤立的原DO/Mapper/Converter/XML；trade补system-client依赖。
3. 新库不创建旧配置表；提供sys_config结构升级、旧库数据复制、两个key新库默认种子。复制保留member首条配置、trade的id=1，保留来源审计；目标已存在key不覆盖。旧表不自动删除，用作核对与回滚。
4. 测试：system真实Mapper/H2验证长JSON、唯一键、原子upsert和审计；业务Gateway验证缺失、故障和JSON；原有member/trade集成夹具改为显式RPC测试替身，不能声称为实际Dubbo验收；回归现有HTTP合同和业务校验。
5. 同步迁移说明、索引、交付和证据；本轮不执行运行中数据库迁移、不部署、不commit/push。

## 发布和回滚

先停配置写入并备份；检查sys_config重复key，不能自动删除冲突数据。旧库先升级sys_config结构，再复制旧表值，再执行默认种子（不覆盖）；最后先发布system再发布member/trade。服务账户只需访问各自数据库，跨库复制由发布时的数据库管理账户执行。复制SQL默认库名须按实际环境调整。旧表不会继续双写，切换后回滚需先回填sys_config最新值，不能直接恢复陈旧旧表。

技术沿用JDK17、Spring Boot4.1.0、MyBatis-Plus、Dubbo、Fastjson2及Maven；不引入新框架。真实MySQL执行和部署后RPC/HTTP单列为未验证项。下一接收方为工程实现及QA/用户验收。
