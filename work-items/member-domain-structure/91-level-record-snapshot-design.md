# 等级变更快照设计

2026-10-30，F04，源MemberLevelRecordDO、copyTo与手工调整代码。源experience自动copy路径为等级门槛，手工路径覆盖为经验差值。按源实际逻辑保留，不自行统一解释。现API experience仍为变更后会员经验，新增recordExperience表示源记录值，附加level/discountPercent/description。

数据库原experience（旧会员余额）改user_experience保留，新增experience为源路径记录值；member_id→user_id、reason→remark。旧记录源值/等级/折扣不按当前配置回填。自动有目标等级取配置门槛；手工取target-oldExperience，取消为-oldExperience。当前已有的自动取消等级行为不在本批改变，无码可匹配时记录已确认经验delta，并明示该状态机差异未迁移。

新记录等级/折扣取变更当时配置，手工描述用源文本，自动描述保持空；LevelRecordDO复用本地BaseDO/审计。后台列表/详情/用户后台列表独立Res及React映射新字段；无App记录接口新增。测试配置变更后快照稳定、取消差值、自动150余额/100门槛、旧结构升级保留原余额原因及未知源值。只生成脚本，不执行真实库，不commit/push。
