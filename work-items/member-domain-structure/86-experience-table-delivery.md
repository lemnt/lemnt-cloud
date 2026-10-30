# 经验独立表交付

2026-10-30，承接85。新增member_experience_record，源经验列和数值业务类型建模，独立DO/Mapper/XML resultMap/Gateway/Converter；所有经验请求、写入、分页/count、详情及售后累计走新表。旧资产Gateway只路由，旧AssetRecordDO/Mapper/Converter/XML移除，新库不再创建member_asset_record。积分和经验编号独立，按类型定位。

经验元数据独立映射管理员0、签到4、订单奖励11、售后回退13，写入时保存源标题描述。pointBizType不借给经验，新增experienceBizType；原reason/业务键保留。后台和App经验独立响应、React及小程序展示，编号0保持有效，旧未知不补造。BIGINT保留当前long范围，源审计列/最终数值HTTP命名/应用兼容接口和零变更标记等差异仍未完整收敛。

member/auth/mall/trade clean verify通过：member41/auth18/mall118/trade158，共335项失败/错误/跳过0。首次增量构建残留旧XML导致加载失败，清理产物后完整构建通过。原并发幂等、签到双表事务/失败回滚、自动升级及交易退款累计继续通过；新增经验元数据/重试及实际H2复制脚本测试保留旧ID/NULL/原因、同ID隔离、旧请求不重复余额。React196项与类型/代码/格式/构建通过，小程序118项通过，均验证经验0及独立标题。

旧库仅生成V5建表和copy脚本，需停写备份、核对字段/数量/内容/自增后切代码，脚本不删除归档。未真实MySQL升级、部署HTTP/浏览器/真机验收，未commit/push。文章继续排除。

接口docs/member-point-record-filter-api.md；证据evidence/experience-table-verification.json。
