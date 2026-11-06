# 等级变更快照交付

2026-10-30，承接91，补齐等级记录的变更时等级、折扣、源经验记录值和描述；持久层按源列名映射user_id、user_experience、experience、remark，复用本地BaseDO。后台三份独立响应及React列表接入快照。

保留现有接口experience表示变更后会员经验，新增recordExperience保存源实际值：自动升级为目标等级经验门槛，手工调整为目标经验减原经验，取消为负原经验。源两条路径含义不同，页面使用“记录经验值”并解释，未强行统一成门槛。修改等级配置后历史快照不变，原请求重试不重复记录。

旧库升级脚本将原余额experience重命名user_experience，原member_id/reason改user_id/remark；新增快照及审计列保持未知NULL，不用当前配置补历史。新建库V2同步更新。真实H2用例执行了旧结构升级并读回原余额/原因及未知快照；真实MySQL升级、外键索引、部署HTTP未验收。脚本须在维护窗口备份核对结构后仅执行一次，已有库不能重跑初始化V2。

最终member/auth/mall/trade Maven verify通过，46/18/118/158共340项测试，失败/错误/跳过0。新增3项覆盖配置修改与重试稳定、手工取消差值、自动余额150与门槛100分离、真实旧结构升级。React197项通过，build、lint、format:check、typecheck通过；构建仅有大chunk提示。小程序未改，本批未重跑。

未commit/push，未执行真实库。当前项目自动无匹配等级时取消等级的状态机与源不同，本批保留并列为待迁移；源逻辑删除及完整接口收敛也未宣称完成。文章仍排除。接口说明见docs/member-level-record-api.md，证据见evidence/level-record-snapshot-verification.json。
