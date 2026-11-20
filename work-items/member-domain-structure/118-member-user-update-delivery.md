# 后台会员资料保存交付

2026-10-31，承接117，参考本地芋道MemberUserServiceImpl.updateUser及MemberUserBaseVO。

后台PUT /member-users/{id}通过独立MemberUserUpdateCmd/Exe维护手机号，省略/null保留旧值，空串非法；复用现有MemberRules.mobile，不新增短信微信。React编辑提供手机号字段，字符串保留+前缀；请求白名单继续排除资产/等级分组标签及返显名称。App保留原命令和本人ID来源，附加后台JSON字段不会进入命令。

同一事务锁定会员后校验手机号/邮箱唯一，自有值允许，他人占用409。既有Lambda局部资料更新加入手机号；数据库唯一约束冲突转换为统一ServiceException，其他资料不得部分保存。密码、资产、等级分组标签、创建及登录审计不覆盖。未新增表/列/Mapper CRUD方法/依赖。邮箱省略清空仍为旧接口语义，不在本批改变。

新增3项H2验证改号查询、自有值、省略和App保留、密码资产关联及登录审计保持、占用/非法失败原值不变、真实唯一约束冲突；2项MockMvc验证后台/App命令隔离及边界校验；1项React验证手机号字符串和省略/null/空串。数据库约束为真实H2执行，未运行并发线程竞争。

四服务联合verify通过：member103、auth18、mall119、trade158，共398，失败/错误/跳过0；React222项、35文件，小程序123项通过。前端build/typecheck/lint/format:check通过，仅既有大chunk提示；git diff --check通过。日志/tmp/member-user-update-*.log，证据evidence/member-user-update-verification.json，接口docs/member-user-update-api.md。

源普通编辑声明标签分组等级，本项目保留专用操作及等级记录；源手机号必填，当前暂兼容省略。源禁用撤销Token需按auth/网关边界继续对齐；用户完整契约仍in_progress，不宣称全量迁移完成。

无DDL，未commit/push，未执行真实MySQL/Redis/Nacos/部署HTTP/浏览器或微信验收。
