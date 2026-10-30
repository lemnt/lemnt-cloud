# 积分记录剩余筛选交付

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer；承接99，源后台MemberPointRecordPageReqVO/Mapper、AppMemberPointRecordPageReqVO/Mapper和uniapp score.vue。

后台独立Req增加title，按源包含匹配标题快照，与昵称、会员、数值源类型及兼容业务码组合；React积分标题可搜索。源后台无业务编号/时间条件，本批未自行增加。历史NULL标题不借用原因参与匹配。

App独立PointRecordPageReq增加addStatus、时间起止；应用保留false，积分>0为收入、<0为支出、NULL为全部含零流水；时间闭区间/可单边，逆序拒绝。会员ID仍取网关上下文，外部ID无效；经验调用不能借用积分筛选。现有内部Qry/领域Filter保留旧构造兼容，XML列表/总数共用条件并保留resultMap。数据库/响应和积分写入不变。

小程序会员权益积分记录增加收入/支出切换、日期起止和重置；日期扩展到首秒/末秒，按已应用条件翻页/刷新，日期草稿不影响当前分页，切换记录类型清空。service只取白名单，false保留，不传筛选里的会员编号。详情见docs/member-point-record-filter-api.md。

member/auth/mall/trade离线verify通过：62/18/118/158，共356项，失败/错误/跳过0。新增2项H2集成测试：后台标题包含/昵称类型组合及分页count、历史NULL不借原因、会员隔离；App正负零、时间端点/单边、分页总数、逆序/跨资产拒绝。新增2项MockMvc覆盖后台标题绑定/长度422，以及App false/日期绑定和攻击者memberId不替换可信身份。

React202项通过，build/lint/format:check/typecheck通过，构建保留既有大chunk提示。小程序122项Node测试通过（新增service2/page2），覆盖false、参数白名单与隔离、无效条件不发请求、已应用日期跨页稳定及切换/重置。git diff --check通过。证据evidence/point-record-remaining-filters-verification.json；日志/tmp/point-filters-verify.log、/tmp/point-filters-ui-*.log、/tmp/point-filters-mini-tests.log。

未执行真实数据库、部署HTTP、浏览器E2E或微信开发者工具验收；未commit/push。仍有泛化资产应用契约、最终接口命名、零经验与退款入口等源差异；本批不宣称完整会员迁移，文章排除。
