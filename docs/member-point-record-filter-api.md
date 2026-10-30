# 后台积分流水筛选

2026-10-30，对照源MemberPointRecordPageReqVO的昵称/会员/业务类型能力。

GET `/member/admin/point-records`，权限member:point:query，分页R.rows/R.total保持。独立请求字段：memberId可选正ID；nickname最多80字包含匹配；bizType最多40字按当前存储代码精确匹配（如ADMIN、SIGN_IN）；pageNum默认1最大1000000，pageSize默认20最大100。字符串空白忽略，多个条件同时生效，无匹配空页。超长参数HTTP422、应用400。

后台分页项新增可空nickname，来自当前member_user昵称，非历史快照；其余id/memberId/assetType/delta/balance/bizType/bizNo/reason/createTime保留。原name/status通用请求定义未生效，本批去掉。固定查询POINT，经验和会员App各自独立HTTP请求不增加这组筛选条件。

复杂关联分页与count用XML同条件、完整resultMap，普通资产写入和余额调整不变。前端积分页提供昵称、业务类型查询，数值/原因/时间仅展示；签到昵称搜索从统一关闭搜索的配置中分离修正。

尚未完成的源差异：源bizType是数值枚举，当前仍为字符串；源title不是现reason，本批未假装映射。源积分表字段与表划分的完整对齐另行迁移，不称本批已全量复刻积分模块。

无DDL或新依赖，未部署HTTP/权限切面/浏览器验收。

## 积分业务展示快照（2026-10-30）

新积分流水增加可空pointBizType/title/description，分别为源枚举编号、积分标题和描述快照。后台积分分页/详情及本人积分分页独立响应增加这三个字段；经验响应不增加。reason保留原人工/业务原因，不作为标题或描述的替身。旧记录不回填，无元数据保持空。

明确映射：SIGN_IN=1，ADMIN=2，TRADE_USE=11，TRADE_USE_CANCEL=12，TRADE_USE_RETURN（售后）=13，TRADE_GIVE=21，TRADE_GIVE_RETURN（售后）=23。源22当前没有对应业务调用，不生成；零变更TRADE_CLOSED内部标记和未知来源不赋枚举。描述模板沿源文本，变更量带符号，不改余额和原幂等键。原请求重试返回原保存快照。

这是兼容阶段：bizType仍是旧字符串、查询参数仍按原码筛选，pointBizType为附加只读元数据；未完成源独立积分表和数值bizType契约迁移，不能称最终字段对齐。member_asset_record新增point_biz_type/title/description可空列；旧库point_record_metadata.sql仅生成未执行。后台和小程序展示标题描述，历史未知不按当前规则或原因补造。

## 源数值类型筛选（2026-10-30补充）

后台积分请求新增可选pointBizType，取值1、2、11、12、13、21、22、23，和memberId/nickname/原bizType条件同时生效。非法枚举值400，非数字/范围校验422。22虽无当前写入来源，仍是合法源枚举筛选值。

指定数值仅匹配实际保存的point_biz_type；历史NULL不会按旧字符串业务码临时推断。未提供条件时保留历史记录。经验请求不新增该参数，应用层也拒绝用积分数值过滤经验。列表/count共用条件。

后台业务类型列改为源枚举选择及中文名称；旧字符串输入标为“原业务码”保留兼容。筛选能力已补齐，但源最终bizType数值字段名/独立积分表仍未替换，不能称表结构全量对齐。无新DDL或写业务变更。

## 积分独立表（2026-10-30）

POINT现读写member_point_record，DO/Mapper/Gateway/Converter独立；source业务列user_id/biz_id/biz_type/title/description/point/total_point/create_time已建模。数值biz_type保存源编号，原业务键存biz_code，原原因存reason，两列为兼容用途；积分数值保留BIGINT，不截断既有long。旧未知枚举仍可空，源审计基类字段及最终HTTP bizType数值命名尚未完整替换。

既有应用资产Gateway暂作兼容路由：积分请求、保存、会员分页、后台筛选、count、详情与售后累计都走独立PointGateway；经验仍旧资产表。详情按类型和ID定位，两表ID可相同，不跨表搜索。新库member_asset_record约束只允许EXPERIENCE，积分不双写。签到积分/经验仍同一个会员数据库事务。

旧库停写并备份后，确认point_record_metadata列升级已执行，单独执行V4__member_point_record.sql创建新表，再执行upgrade/point_record_split.sql复制。保留旧ID、业务键和NULL元数据；脚本不删旧POINT数据，也不回填现在的枚举。核对行数、逐项内容、自增序列与幂等查询后再切换代码；旧POINT只作归档，后续清理另审。本批只在H2验证，不执行真实MySQL。不得重新运行整套新版初始化覆盖已有库。

表拆分已实现，不代表迁移到源项目的全部字段/审计/最终请求名完成。HTTP兼容形状未变，本批无前端变更。

## 经验独立表（2026-10-30）

EXPERIENCE现读写member_experience_record，按源业务列user_id/biz_id/biz_type/title/description/experience/total_experience/create_time建模，独立DO/Mapper/XML resultMap/Gateway/Converter。biz_code/reason保留原幂等码与原因，数值BIGINT不截断现有long。

新经验流水映射ADMIN/ADMIN_LEVEL=0、SIGN_IN=4、TRADE_EXPERIENCE=11、TRADE_EXPERIENCE_RETURN（售后）=13，保存源标题描述。尚无当前来源的邀新、抽奖、整单取消不生成。后台经验分页/详情和本人经验分页新增experienceBizType/title/description；pointBizType仍只表示积分，0是有效经验类型。旧未知快照不回填，不拿积分元数据或reason当经验标题。后台和小程序展示独立经验内容。

原AssetRecordGateway暂仅为应用兼容路由，已不持旧表Mapper。旧AssetRecordDO/Mapper/Converter/XML移除，新库V2不再创建member_asset_record。积分和经验ID各自独立，所有详情按类型定位。旧库不能重新运行新版V2；停写备份后创建V5，再执行experience_record_split.sql，只复制旧EXPERIENCE、保留ID/原键/原因，未知源元数据置空；旧表留归档，核对数量/内容/自增后切代码。真实升级未执行。

尚未完成源审计基类全部列、最终HTTP数值bizType命名、应用兼容契约及旧归档清理。现有零变更幂等标记仍保留，未将当前流程差异伪装为源完全一致。

## 泛化网关退役（2026-10-30）

MemberAssetRecordGateway及其基础设施路由实现已移除。成长编排直接注入MemberPointRecordGateway与MemberExperienceRecordGateway，内部按业务类型选择请求、写入和退款累计；三个查询执行器直接依赖逐表网关。没有另建同义路由服务。

逐表save拒绝与账本不符的assetType，避免现有共享投影被误写另一张表；详情非法资产类型明确拒绝，合法同ID仍按自身账本读取。会员锁、幂等键、外层事务、HTTP和数据库均未改。既有AssetRecord/Qry/DTO作为应用兼容投影仍保留，最终HTTP业务类型字段命名尚未收敛。

## 两张流水表基础审计（2026-10-30）

PointRecordDO、ExperienceRecordDO继承本项目BaseDO，不再自定义createTime遮蔽父字段。按system规范使用create_by/create_time/update_by/update_time，未采用另一个creator/updater命名体系。XML resultMap同步映射；API不暴露这些新增内部字段。

新流水保留业务传入的数据库createTime，将初始updateTime设为同一事件时间；操作者使用既有MyMetaObjectHandler及安全上下文，缺少用户名时system表示服务执行身份，不反推会员姓名。幂等原请求返回旧记录，不更新审计。未修改公共填充器或引入流水修改/删除能力。

新库V4/V5含可空审计列；已有独立表仅在确认列缺失后执行upgrade/ledger_audit_fields.sql，不能对新版初始化重复执行。旧数据及旧表copy保持未知NULL，不回填当前用户或时间。真实库脚本未执行。源deleted字段及不可变账本的逻辑删除语义本批未引入，不能把基础审计对齐当成源所有基类字段完整迁移。
