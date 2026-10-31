# 积分业务元数据快照交付

2026-10-30，承接79设计。新增积分元数据映射PointRecordMetadata，明确业务映射源1/2/11/12/13/21/23，标题和带符号变更描述按源模板写入保存。源22无对应来源不生成，经验、未知来源及TRADE_CLOSED内部零变更标记保持空，不猜测。reason原原因保持独立，原字符串业务码和幂等键不改。

member_asset_record增加三个可空列，domain/DO/DTO及独立后台积分分页/详情、App积分分页Res贯通，经验响应不增加。后台及小程序展示标题描述。旧记录不回填，旧请求重试不重新生成快照。升级脚本仅生成未执行。

member/auth/mall/trade相关verify通过，会员37、认证18、商城118、交易158，失败/错误/跳过0。新增真实H2业务写入/重试快照/余额不重复/原因分离/经验隔离及枚举映射测试。React194项及类型/代码/格式检查、构建通过；小程序117项通过，新用例验证标题描述/原因分离、经验和历史未知不补造。未部署真实数据库/HTTP/浏览器/真机，未commit/push。

兼容阶段未完：仍使用泛化资产表和字符串bizType，新增pointBizType只读元数据未替代原筛选；源独立积分表和最终数值业务类型契约仍待迁移。不能把本批说成积分全量对齐。文章继续排除。

接口docs/member-point-record-filter-api.md；证据evidence/point-record-metadata-verification.json。
