# 积分业务元数据快照设计

2026-10-30，承接78，源MemberPointBizTypeEnum及createPointRecord。当前字符串业务码用于权益幂等，不能直接替换成数值导致原请求查不到；本批保留bizType，新增pointBizType/title/description只读快照，数值作为兼容阶段字段，不称源表/API最终对齐。

明确映射：SIGN_IN→1、ADMIN→2、TRADE_USE→11、TRADE_USE_CANCEL→12、TRADE_USE_RETURN（售后）→13、TRADE_GIVE→21、TRADE_GIVE_RETURN（售后）→23。22源整单取消赠送回退当前无独立来源，不生成；TRADE_CLOSED零变更终态标记及经验不赋枚举，不把原因reason当title/description。标题、描述按源枚举文本与带符号delta在写入时保存，退款是否最终全额不改变“售后”来源语义。原reason/幂等键保持。

member_asset_record新增可空point_biz_type INT/title VARCHAR255/description VARCHAR500；旧记录不按现在规则回填，历史未知保持NULL。独立积分后台分页/详情及App积分分页响应附加字段，经验独立响应不增加。前端积分记录展示标题/描述与源数值类型（只读），筛选仍沿78原bizType；枚举筛选/表拆分后续处理。

测试实际业务写入及原请求重试快照稳定、映射、不改reason、经验和未知来源NULL，H2读取/字段转换、前端与小程序解码。仅生成升级，不commit/push、不执行真实库。
