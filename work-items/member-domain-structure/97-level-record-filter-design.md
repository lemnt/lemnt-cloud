# 等级记录后台筛选设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接96。源MemberLevelRecordPageReqVO/Mapper使用会员编号、等级编号和创建时间范围。

独立MemberRecordLevelRecordsReq移除未生效name/status，新增可选正levelId及startTime/endTime（yyyy-MM-dd HH:mm:ss，闭区间，支持单边，逆序拒绝），保留memberId及分页。Qry/领域Filter传递相同条件，旧三参数构造保持现有内部调用；HTTP路径权限、统一分页、独立响应字段不变。

等级记录为单表查询，沿用MyBatis-Plus Lambda。page/count使用同一条件；Mapper删除重复insert/按编号查询封装并直接调用BaseMapper.insert/selectById，会员下列表/计数复用等级记录筛选，避免重复SQL。无需XML或表结构变动。

React等级记录增加等级编号与日期范围筛选，切换记录类型清空查询，参数仅发送等级接口。筛选依据记录level_id，不关联当前等级配置，不回填取消或旧记录NULL。

验收真实H2等级+会员+时间组合筛选、分页总数及倒序、取消NULL只在未指定等级时可见、单边边界/会员隔离、逆序与非法编号；MockMvc绑定与参数验证；前端参数及记录类型隔离。运行member/auth/mall/trade verify和UI测试/构建/检查，不commit、不部署。
