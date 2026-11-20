# 收货时间迁移设计

2026-10-29，系统评价的前置字段，参考芋道按COMPLETED/commentStatus=false/receiveTime超期筛选。当前shipment.receivedAt已有真实收货事实，但trade_order缺receive_time。本批先将源订单字段及原子状态迁移补齐，不提前开启自动评价任务。

订单收货事务使用同一数据库时间写物流收货时间与订单receive_time，订单SHIPPED→COMPLETED通过Lambda条件更新同时完成。重复收货保留首次时间；日志失败或状态冲突，物流/订单时间及状态一起回滚。旧记录未知保留NULL，不从下单或评价finish_time推断。仅生成升级脚本；自动评价默认内容、系统身份和任务后续补齐，不能宣称本批完成自动评价。

验证：双H2生产组件的收货幂等、时间一致和失败回滚，相关Maven回归。无HTTP/前端字段变更、不commit/push、不执行真实库。
