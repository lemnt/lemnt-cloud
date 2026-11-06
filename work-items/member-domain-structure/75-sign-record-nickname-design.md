# 后台签到记录昵称筛选设计

2026-10-29，F04，源MemberSignInRecordServiceImpl.getSignInRecordPage昵称筛选及Convert拼昵称。现Req通用name/status未使用，替换为独立nickname可选最长80字，与memberId同时生效。空白忽略，无匹配返回空页。保持GET /member/admin/signin-records、权限、分页及写业务不变。

签到Mapper复杂关联用XML（完整resultMap），关联member_user的当前昵称，不存历史昵称快照；分页和count共用条件。域/应用投影及后台Res附加nickname，App独立Res不增加，普通签到记录仍保持原契约。前端只在签到类型展示昵称筛选并移除不生效的日期/奖励筛选控件。无新表/DDL/远程RPC。

验证真实生产H2/XML：重名、memberId组合、无匹配、分页total及昵称变更；App契约不变。前端查询传参/展示解码回归；未部署HTTP/浏览器、不commit/push。
