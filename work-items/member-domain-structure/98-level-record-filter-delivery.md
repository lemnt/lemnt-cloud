# 等级记录筛选交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer；承接97，源MemberLevelRecordPageReqVO与MemberLevelRecordMapper。

独立等级记录Req去掉未生效name/status，新增等级编号精确匹配和创建时间闭区间（支持单边），会员/分页及原路径权限、独立响应和统一分页不变。Qry与领域Filter贯通，旧构造保留内部调用。应用校验正编号及时间顺序，查询不关联当前等级配置，取消记录的NULL不会被指定等级条件匹配。

单表查询使用MyBatis-Plus Lambda，列表/总数共用同一条件。移除Mapper重复插入、按编号查询封装，网关直接BaseMapper.insert/selectById；会员下列表和计数复用同一过滤路径，减少重复方法。未新增XML、SQL或数据库列。

React等级页提供等级编号、时间范围，编号保留字符串防止大整数精度丢失；切换记录类型已有重建表单及清空查询继续生效，等级编号不发到经验/积分请求。docs/member-level-record-api.md已补说明。

member/auth/mall/trade离线verify通过：58/18/118/158，共352项，失败/错误/跳过0。新增2项H2持久化测试验证等级+会员+时间筛选、倒序分页及count、时间端点/单边、取消记录保留及筛选排除、其他会员隔离、未知编号空页、非法编号与逆序拒绝；2项MockMvc覆盖Req绑定、统一分页及验证失败不调用查询。既有等级快照、幂等、经验归零、旧表升级与交易权益测试继续通过。

React201项通过，build/lint/format:check/typecheck通过；构建仅有既有大chunk提示。新增前端请求测试覆盖大编号原值、日期范围、参数隔离、非正编号拒绝及未填不默认等级。git diff --check通过。证据evidence/level-record-filter-verification.json，原始日志/tmp/level-filter-verify.log和/tmp/level-filter-ui-*.log。

真实数据库、部署HTTP和浏览器E2E未验证，小程序本批未改未重跑。未commit/push。会员域泛化资产投影、最终接口收敛及此前记录的零经验/退款入口差异仍未完成，未宣称完整迁移，文章排除。
