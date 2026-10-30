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
