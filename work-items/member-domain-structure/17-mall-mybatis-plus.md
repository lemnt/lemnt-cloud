# Mall MyBatis Plus 与 XML 规范调整

## 用户要求

MyBatis Plus 已提供的 CRUD 不重复定义；条件操作优先 Lambda，无法清晰表达的 SQL 放 XML，不使用 JPA。

## 实现

- 27 个按表 Mapper 保留 BaseMapper 继承，删除 54 个默认包装方法及 25 个可直接用基础 CRUD / Lambda 表达的注解方法。
- GatewayImpl 直接调用 insert、selectById、deleteById、selectOne、selectList、selectCount、selectPage 和 update；条件通过 DO 方法引用构造。
- 购物车版本、库存归还及促销计数变化使用 setIncrBy / setDecrBy；保留原有会员、订单、版本和状态条件。
- 商品列表和统计复用过滤条件；分页直接使用 MyBatis Plus 分页对象及其总数，更新保留空值清除语义和已删除状态保护。
- 多表查询、带原子限额的库存/促销更新、upsert、数据库时间和显式行锁放入 17 个 XML，合计 36 条自定义语句；Java Mapper 无 SQL 注解、SQL 常量或 default 包装方法。
- 行锁语句明确 flushCache=true、useCache=false，防止重复加锁查询被缓存替代。
- XML 平铺在 infrastructure/src/main/resources/mapper/，兼容已有 Nacos 的 classpath:/mapper/**.xml 配置；手动装配的测试工厂同步加载真实 XML。
- 新增 MapperXmlLoadingTest，验证上述生产扫描路径、所有自定义语句绑定、继承 CRUD 的加载及行锁缓存属性。
- 项目 AGENTS.md 记录持久化约定。没有引入 JPA，也未修改业务表或 HTTP/RPC 契约。

## 验证边界

结果见 evidence/mall-mybatis-plus-verification.json。真实 Mapper/XML、H2 MySQL 模式、应用服务和事务代理覆盖商品、规格、购物车、领券、结算、促销、库存和并发重试回归；未连接实际 MySQL 做方言验收，未部署服务。不提交或推送代码。
