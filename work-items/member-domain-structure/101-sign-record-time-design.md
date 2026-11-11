# 签到记录时间及筛选迁移设计

需求member-domain-structure，2026-10-30，版本1，责任code-software-engineer，承接100。源MemberSignInRecordDO/PageReqVO/Mapper包含member_sign_in_record、user_id/day、BaseDO.createTime及day/创建时间筛选。当前只有业务日期，不能用于冒充真实时间。

新库V2统一记录表为member_sign_in_record、user_id/day，保留sign_date与会员日期唯一约束供当前签到幂等逻辑；奖励保留BIGINT。DO继承本地BaseDO，模型/DTO及四份独立Res新增可空createTime。真实签到使用同一次数据库now按现有DATETIME秒精度截断纳秒，作为业务日期和事件时间，初始updateTime一致，重试不改旧记录。源deleted及sign_date冗余收敛本批不迁移。

旧库升级一次：表/列改名，新增可空create_time和审计列；旧行保持NULL，不由sign_date拼午夜，不补当前时刻/操作者。新库不能重跑升级，真实MySQL未执行；需停写备份核对唯一键/外键后部署。

独立后台Req/Qry/Filter新增可选正day和startTime/endTime（yyyy-MM-dd HH:mm:ss，闭区间/可单边/逆序拒绝），保留会员、昵称、统一分页和权限路径。XML昵称关联查询继续使用resultMap，列表/总数共用条件；基础Mapper插入直接BaseMapper，不增重复CRUD。React提供day及时间筛选，返回真实时间；小程序仅展示已知时间，缺失仍只显示业务日期，App入参不加后台筛选。

验收H2真实签到时间/重试稳定、筛选组合及count/边界、未知时间保留和升级SQL执行；MockMvc绑定/验证；React参数隔离及NULL时间；小程序已知/未知时间。运行相关全后端verify、React检查和Node小程序测试；不commit、不执行真实库、不部署。
