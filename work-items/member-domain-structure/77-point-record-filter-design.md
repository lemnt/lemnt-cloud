# 后台积分流水筛选设计

2026-10-30，F04，参考源MemberPointRecordPageReqVO.nickname/userId/bizType及昵称拼接。现独立积分Req的name/status未使用，替换nickname(80)/bizType(40)。与memberId组合，空白忽略；业务类型按当前已存字符串精确匹配，不把源数值枚举假装已迁移。源title与现reason不等义，本批不映射，仍待迁移。

复杂关联XML/resultMap，读member_user当前昵称，count与分页同条件，固定POINT；查询层共用资产模型仍严格区分类型，经验/会员App独立ReqRes不加这些条件。后台积分Res附加nickname；不改资产写入、余额或幂等。前端积分类型增加昵称/业务类型筛选，金额/时间等未实现条件只展示。

验证真实H2/XML组合、分页、无匹配、类型隔离，HTTP请求及超长拒绝、前端传参和回归。无DDL/新表/远程RPC，不commit/push。源积分title/数值枚举及单独表字段的完整对齐仍未完成。
