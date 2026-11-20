# 积分配置管理闭环设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接106。依据源MemberConfigServiceImpl/DO、TradePointUsePriceCalculator/TradePointGiveCalculator及当前mall计算。后台实际/growths/config，当前前端旧/member/admin/config不匹配；maxPrice实为每单可用积分上限，UI误标金额；现只update固定id1，缺失时可假成功。

存储按源首条配置读取/更新，空表首次插入；ID改Long自增，DO继承本地BaseDO，移除Mapper重复CRUD及注解SQL。保持四项外部契约，升级原ID不改，历史审计NULL。新V2保留现有明确默认配置seed，旧库脚本一次执行，真实MySQL未执行。

移除ConfigGateway.lockCatalog及配置行充当目录锁；新增growth域配置锁适配基础lemnt-common-lock的LockTemplate/Redisson，等级配置与积分配置各次事务持锁到提交/回滚。固定配置锁键，同事务不重复获取；空表也可锁，防首次保存并发多行。基础锁失败统一409，不加自定义锁表。基础Redis锁实际环境未验收，测试LockTemplate替身配合真实H2事务。

UI读取/保存实际/growths/config、只发四项内容。GET null作为未配置状态可显示编辑草稿，只有用户保存才持久化，不回填既有历史。提示已接入结算，最大抵扣单位是积分而非分，0不限；关闭开关按源同时停抵扣和按实付赠送。服务客户端未配置时明确409，避免NPE，不复制源缺失配置异常。

验收H2首条任意ID更新/空表首次保存/并发单行、失败回滚、基础锁事务完成释放及拒绝超时、旧结构升级保持ID/字段/未知审计；等级配置不依赖积分行。MockMvc和Dubbo读回配置，mall用单积分5分、最多2积分验证实际0.10元抵扣及开关赠送行为。React路径/白名单/未配置草稿/正确单位，完整后端verify、UI和小程序回归。不commit、不部署。
