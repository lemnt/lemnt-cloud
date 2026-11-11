# 后台停用会员与会话撤销

后台 `PUT /member-users/{id}` 明确传入 `status: 0` 时，资料修改与同步通知auth属于一次保存流程。原接口、权限及返回值不变。继续保存已停用状态也会再次撤销旧会话；启用保存与App资料更新不会发起该远程操作。

member通过MemberAuthGateway调用既有auth-client的 `MemberSessionClient.revokeMemberSessions(memberId)`。Token操作在auth中执行，member不解析token、不读取会话缓存；gateway仍复用TokenService，不引入Dubbo。auth保留无数据库设计。

TokenService使用公共Redis封装SCAN匹配LOGIN_TOKEN_KEY，逐项读取SecurityUser，只删除真实userType=member且userId匹配的会话。同ID管理员、其他会员和未知/已到期会话不删除。不新增Redis Set/用户会话索引、表或自算token。当前集合索引已移除，因此停用需要遍历现有会话，实际耗时随全局会话量变化；生产Redis集群的游标覆盖和性能尚未验收。

后台在会员行锁内写资料、同步调用auth、成功后提交。auth撤销RPC仅访问Redis，不回调member，避免等待同一会员事务。调用失败时本次资料、状态、手机号更新回滚；客户端收到失败。Redis撤销与数据库提交不具备分布式原子性：若撤销后提交失败，旧会话依然失效，仍启用会员需重新登录。

会员刷新仅用原子SET XX更新仍存在的会话并续期，已删除会话不能被读取旧对象的并发刷新重新创建。签发新token继续原公共创建逻辑。与停用同时登录时，既有recordLogin锁定检查会拒绝已停用会员，并由auth撤销新签发会话。

该批补齐源MemberUserServiceImpl.updateUser禁用后撤销会话的行为。源码测试包含H2事务回滚、auth多终端及身份隔离、旧对象续期、网关旧token拒绝、Redis封装的游标关闭和SET XX调用。Redis调用为Mockito替身，无真实Redis/Nacos/Dubbo/部署HTTP或端到端验收。
