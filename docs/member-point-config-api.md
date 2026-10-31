# 会员积分配置闭环

2026-10-30。GET/PUT `/growths/config`，权限分别member:config:query/update；四项独立Req/Res和R封装不变。前端已修正实际路径，保存仅发送四个业务字段。

|字段|含义|
|---|---|
|pointTradeDeductEnable|启用积分抵扣及按实付赠送积分；关闭同时停用两者|
|pointTradeDeductUnitPrice|每1积分抵扣多少整数分；启用时必须大于0|
|pointTradeDeductMaxPrice|源历史字段名，实际是每单最多使用积分数，0不限；不是金额|
|pointTradeGivePoint|每支付1元赠送积分数，向下取整|

例：每积分5分、每单上限2积分，余额100且实付前10元，则最多抵扣0.10元，实付9.90元；每元赠3积分时获得29积分。当前mall计算与芋道源码一致，本批补测试和界面单位，不改正确的结算算法。配置变更参与后续重新报价，已存结算/订单快照沿原交易规则处理，不声称改写历史订单。

GET无配置返回null，后台提供明确的未保存草稿，由用户保存才写入。保存按源读取真实首条ID进行更新，空表首次插入，避免固定ID=1或更新0行却返回成功。配置主键Long自增，DO复用本地BaseDO；历史ID/字段保留，审计未知NULL。RPC未配置时明确409，避免空指针。

原配置行用作等级目录锁的代码移除，使用基础lemnt-common-lock的LockTemplate/Redisson事务锁，锁键lemnt:member:growth:configuration。等级/积分配置在提交或回滚后释放，同一事务不重复获取；首次保存并发只生成一行。部署需要配置基础Redis/Redisson锁连接，本批仅替换Redis执行为测试模板并验证真实H2事务，未验证实际Redis集群。

新库V2保持原有默认seed。旧库备份核对后一次执行sql/upgrade/point_config_audit.sql，ID类型改自增BIGINT且保留原ID，新增审计NULL；真实MySQL未执行。网关/growths路由示例承接106尚未应用Nacos。详见108交付及验证JSON；部署HTTP、浏览器和微信端未验收。
