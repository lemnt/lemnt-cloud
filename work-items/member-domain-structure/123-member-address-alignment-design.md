# 收货地址源逻辑收敛设计

2026-10-31，承接122；参考芋道AddressServiceImpl、MemberAddressDO、MemberAddressMapper。源没有20条限制，写入前校验地址归属，默认地址仅清除本人其他默认项，缺失/他人地址删除报错。

1. 删除额外20条限制，保留会员行锁使默认切换串行。更新/删除不存在或非本人统一404；查询保持null。删除默认不自动指定其他地址。
2. AddressDO继承项目BaseDO，对齐源user_id；新建表同步，旧表一次性升级仅改属主列名和增加可空审计列，未知历史不回填，不新增业务表。Domain/HTTP/RPC保持memberId映射及原路径/形状。
3. Mapper仅继承BaseMapper，Gateway按表用Lambda完成查询/更新/删除，去掉重复insert包装及简单CRUD包装。更新和清除默认传实体触发统一审计填充；清默认只影响本人其他true项，不触碰普通地址/正在编辑项。
4. 默认地址查询在数据库按user_id和default_status过滤，只返回一条，空时null。

验证H2真实Mapper、20条以上创建、越权/缺失删除、默认切换/删除/并发、审计选择性变更、升级保留旧记录NULL及FK；MockMvc本人上下文和统一异常；业务四服务+gateway/common联合verify，小程序相关回归。无真实库执行/提交，源逻辑删除与最终字段契约保持诚实待核对。
