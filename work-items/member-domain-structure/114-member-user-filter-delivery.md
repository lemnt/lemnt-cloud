# 会员列表源筛选交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接113；源MemberUserPageReqVO/Mapper。

独立Req/Qry与领域MemberPageFilter接通手机号/邮箱/昵称包含、等级/分组、任一标签OR、注册/登录两组时间范围，含单边/闭区间及逆序校验。原手机号精确改源包含；关联ID正数、空标签不筛选、标签去重。列表/count同一XML条件，既有member_tag_link的EXISTS+IN不重复会员，不复制源CSV标签字段。API路径/权限/统一分页和资料独立Res保持，不增返回字段或SQL表。

MemberUserMapper复杂筛选及原数据库时间、行锁迁XML并含完整MemberMap，重复profile包装移除、按ID用BaseMapper；安全资料部分更新/密码查找仍原语义，认证不使用管理模糊查询。当前表字段均已有，本批无DDL。

React采用独立MemberUserPageQuery，接邮箱/昵称、元数据选择和标签多选、两组日期，查询/页码入缓存；元数据请求受相应权限限制。标签大编号保留字符串，Axios明确重复键序列化，四种其他流水筛选不扩入会员请求。展示原注册/登录时间，NULL不补造；网关示例补/member-users/**，未应用Nacos。

member/auth/mall/trade离线verify通过：93/18/119/158共388项，失败/错误/跳过0。新增2项H2验证全组合、部分匹配、标签任一及多命中不重复、倒序分页/count、时间端点/单边、空标签、未知标签/NULL登录及非法ID/范围；新增2项MockMvc绑定全部条件/重复tagIds键与参数422。原认证、会员资料/签到、配置锁和交易权益回归继续通过。

React218项通过，build/lint/format:check/typecheck通过，构建仅既有大chunk提示。新增2项API测试验证参数白名单、大ID字符串、空标签/非法ID不发请求，并实际调用Axios.getUri核验tagIds重复键无[]。小程序123项回归通过。git diff --check通过。接口docs/member-user-filter-api.md；证据evidence/member-user-filter-verification.json及/tmp/member-user-filter-*.log。

未commit/push，未执行真实数据库/Nacos/部署HTTP/浏览器E2E/微信验收。会员资料字段与业务其他源差异仍待后续，不宣称全量完成；文章排除。
