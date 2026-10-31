# 停用会员撤销会话设计

参考芋道 MemberUserServiceImpl.updateUser：后台保存停用状态时同步调用认证中心撤销该会员会话。当前网关直接读取公共缓存，不向member查询状态，不能只禁止auth刷新。

1. 扩展既有auth MemberSessionClient，由auth调用公共TokenService撤销member身份下指定会员的全部会话；member仅通过领域Gateway同步发起RPC，不读取/解析token，gateway无Dubbo。
2. 复用LOGIN_TOKEN_KEY及缓存SecurityUser，公共Redis组件使用SCAN游标逐项处理，无KEYS全量阻塞、无新增token集合索引/表。按真实缓存身份类型及userId匹配，保留同ID后台会话和其他会员会话。
3. 后台updateUser事务保持会员行锁，资料更新后同步撤销再提交，RPC失败资料回滚。RPC仅访问缓存，不能回调会员行造成锁死。认证登录最后recordLogin仍校验锁定后的状态并失败撤销新会话，处理与停用并发的登录。
4. 会员续期使用Redis SET XX带TTL，仅更新仍存在会话；防止已注销/停用删除的会话被并发刷新重新创建。创建令牌仍原SET逻辑。

验证：公共组件类型隔离、SCAN及SET XX；auth多会话撤销/重新启用不恢复旧token/刷新不可复活；member停用成功调用及RPC失败事务回滚、启用/App不调用。联合common+member/auth/gateway/mall/trade构建，区别Mock测试与真实Redis部署证据。

边界：同步RPC与数据库不是分布式原子事务，撤销后本地提交失败会让仍启用会员重新登录；不能声称跨库Redis强一致。无索引方案停用需遍历会话缓存，耗时随全局会话量增长，部署Redis集群拓扑及负载另验。
