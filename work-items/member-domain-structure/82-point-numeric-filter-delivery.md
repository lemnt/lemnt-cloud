# 积分源数值类型筛选交付

2026-10-30，承接81设计。后台独立积分请求新增pointBizType，源枚举1/2/11/12/13/21/22/23，memberId/昵称/原bizType组合AND。无参数包含旧NULL记录，有参数只匹配保存的元数据，不临时从旧码推断；非法枚举/经验混用应用400，非数值绑定422。

XML列表/count使用同条件，既有完整resultMap保持。后台类型列改源枚举选择和名称，原业务码标识兼容，前端只向积分请求传数值。未改写入、余额或历史元数据，无新增表/DDL。

member-start及依赖verify通过，会员38项失败/错误/跳过0；新增H2用例覆盖新快照与旧未知、组合和总数/页大小、无来源但合法22、非法类型与经验隔离；原HTTP用例增强数值绑定、非法枚举/非数字。React195项、类型/代码/格式与构建通过。未部署权限/HTTP/浏览器验收，小程序未改动未重跑，未commit/push。

兼容仍保留：参数名pointBizType未替换原bizType，源独立积分表及最终字段契约未完成；不把数值查询完成当作积分全量对齐。文章继续排除。

接口docs/member-point-record-filter-api.md；证据evidence/point-numeric-filter-verification.json。
