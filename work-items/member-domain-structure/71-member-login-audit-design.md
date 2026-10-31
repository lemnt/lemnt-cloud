# 会员成功登录时间设计

2026-10-29，F04，参考源MemberUserDO.loginDate及MemberUserServiceImpl.updateUserLogin。先补可确认的登录时间；当前无可信客户端IP约定，不伪造loginIp/registerIp或注册终端。auth无数据库，member_user.login_date可空DATETIME，历史NULL不回填。

MemberUserClient增加recordLogin(memberId)写动作（非状态判断接口），provider锁会员核验启用再用数据库时间更新；auth在验证密码且会话创建成功后调用。若审计失败撤销刚建会话并失败返回，避免宣称登录已完整成功；令牌创建失败不写时间。刷新/失败密码不更新。App/认证DTO不暴露后台审计字段，后台分页详情展示loginDate。

逐层字段/转换及独立Gateway/Mapper Lambda写入；不新增表/认证组件。验证成功、失败密码、签发失败和审计失败撤销；member真实H2存储及后台展示，相关回归。IP、注册审计和真实网络仍未完成，不commit/push、不执行真实升级。
