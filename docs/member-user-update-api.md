# 后台会员资料编辑

`PUT /member-users/{id}`，权限 `member:user:update`，返回原 `R<Boolean>`。

已有昵称、邮箱、头像、性别、生日、状态、备注、姓名、地区字段保持；新增可选 `mobile` 字符串。省略或null保留数据库原手机号，非空按项目现有 `MemberRules.mobile`（可选+，7至20位数字）校验，空串不能清空登录手机号。React编辑表单提供手机号输入，不转数值，提交时去除首尾空白。

写入前锁定会员行；手机号、邮箱按真实会员查询校验占用，自己的号码/邮箱允许继续使用。他人占用返回统一ServiceException业务码409，非法手机号400；数据库唯一约束冲突也转409，避免并发时暴露SQL错误。邮箱省略/null沿用原资料接口清空语义，本批不改变。

后台使用独立MemberUserUpdateCmd/Exe；App仍 `PUT /app/users/user`，从认证上下文取本人ID，原Req不含mobile/status/memberId，附加JSON字段不会进入命令。仅写资料字段，不写密码、积分经验、等级分组标签、创建/登录审计；后台省略手机号及App保存都从锁定资料保留原号码。

源依据：本地芋道 `MemberUserServiceImpl.updateUser` 的存在/手机/邮箱校验及 `MemberUserBaseVO`；本项目保留独立标签、分组、等级调整入口，不通过普通资料绕过等级经验记录。源状态禁用后的Token撤销行为尚需按现有auth/网关体系单独对齐，本批没有把Token处理迁回member。源手机号必填，本项目后台HTTP暂允许省略以兼容已部署调用方。SMS/微信按用户范围排除。

无DDL。H2/MyBatis事务及MockMvc为代码层验证，不代表真实MySQL、网关鉴权或浏览器E2E验收。
