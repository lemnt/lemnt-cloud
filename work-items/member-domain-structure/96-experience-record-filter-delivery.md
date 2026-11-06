# 经验流水筛选交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer；承接95，源MemberExperienceRecordPageReqVO/Mapper/业务枚举。

后台独立Req去掉未生效name/status，接入会员编号、业务编号精确匹配、源数值经验类型、标题精确匹配和创建时间闭区间（支持单边）。0管理员类型正确保留，类型集合包含源全部0/1/4/5/11/12/13；未写入的来源不自动造记录。使用专用ExperienceRecordAllPageQry/执行器，保留既有泛化查询作兼容。领域分页条件新增可空经验字段，旧构造不附加条件；沿用带昵称关联XML与resultMap，page/count共用条件，无新增数据库表/Mapper方法。

React经验页增加数值类型枚举、标题、业务编号、时间范围；切换记录类型重置查询并重建表单，经验参数仅发送到经验接口。详情及原响应字段、权限、路由、统一分页保持，App不改。字段说明docs/member-experience-record-api.md。

验证：member54/auth18/mall118/trade158共348项离线Maven verify通过，失败/错误/跳过0。新增2项H2业务持久化测试覆盖组合筛选/精确匹配/类型0与历史NULL区分、会员隔离、翻页总数、时间包含端点及单边、非法类型/逆序范围、源全部类型；2项MockMvc测试覆盖独立Req时间和0值绑定、分页/长度422且不调用查询。组合用例先发现最终源码XML未接入筛选，修正源码映射后完整回归通过。

React199项通过，build/lint/format:check/typecheck通过；build仍仅有大chunk提示。新增请求测试验证0与全部经验字段，以及经验参数不污染积分请求。git diff --check通过。证据evidence/experience-record-filter-verification.json，日志/tmp/experience-filter-source-verify.log与/tmp/experience-filter-ui-final-*.log。

未执行真实数据库、部署HTTP及浏览器E2E；小程序未变更未重跑；未commit/push。泛化AssetRecord投影、最终源接口命名、零经验及退款入口等源差异仍待收敛，未宣称完整迁移。文章继续排除。
