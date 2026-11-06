# 会员积分配置闭环

> 第166批更新：member/mall/trade的旧V*及upgrade脚本已整合移除，本文原执行步骤只作历史说明。新库统一使用[最终初始化文件](database-initialization.md)；已有库另行核对结构差异，system SQL不变。

2026-10-30。GET/PUT `/growths/config`，权限分别member:config:query/update；四项独立Req/Res和R封装不变。前端已修正实际路径，保存仅发送四个业务字段。

|字段|含义|
|---|---|
|pointTradeDeductEnable|启用积分抵扣及按实付赠送积分；关闭同时停用两者|
|pointTradeDeductUnitPrice|每1积分抵扣多少整数分；启用时必须大于0|
|pointTradeDeductMaxPrice|源历史字段名，实际是每单最多使用积分数，0不限；不是金额|
|pointTradeGivePoint|每支付1元赠送积分数，向下取整|

例：每积分5分、每单上限2积分，余额100且实付前10元，则最多抵扣0.10元，实付9.90元；每元赠3积分时获得29积分。当前mall计算与芋道源码一致，本批补测试和界面单位，不改正确的结算算法。配置变更参与后续重新报价，已存结算/订单快照沿原交易规则处理，不声称改写历史订单。

GET无配置返回null，后台提供明确的未保存草稿，由用户保存才写入。第161批改为通过system的sys_config保存member.point.config完整JSON对象，空配置可首次保存；不再依赖原配置表主键。RPC配置缺失时仍明确409，传输故障不降级为默认配置。创建/修改审计由system维护。

等级和签到规则仍使用基础lemnt-common-lock的LockTemplate/Redisson事务锁，锁键lemnt:member:growth:configuration。积分配置整组保存由system数据库唯一key及原子upsert保证，不再持有无关member本地事务锁。

2026-11-02 第161批：新库不再创建member_config，缺失配置的初始种子迁至system；已有库按[业务配置迁移说明](business-config-migration.md)先复制旧值再补默认项。旧表仅保留用于核对/回滚，不继续双写。页面、四字段HTTP合同及原权限不变；不要再对新库执行旧point_config_audit.sql。真实MySQL迁移和部署后RPC/HTTP尚未执行。
