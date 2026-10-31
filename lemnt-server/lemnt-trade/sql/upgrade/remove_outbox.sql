-- 停止旧版 Outbox 调度器、核对旧消息处理结果后执行；本脚本不自动运行。
DROP TABLE IF EXISTS trade_outbox;
