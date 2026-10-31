package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.PayNotifyLog;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;
import com.lemnt.trade.domain.payment.model.PayNotifyTaskFilter;

import java.time.LocalDateTime;
import java.util.List;

/** 支付结果派发任务与日志存储契约。 */
public interface PayNotifyTaskGateway {
    /** 同一 type+bizNo 只入队一次，重复入队返回既有任务。 */
    PayNotifyTask createIfAbsent(PayNotifyTask task);

    /** 按主键读取任务。 */
    PayNotifyTask find(Long id);

    /** 按类型与业务单号读取任务。 */
    PayNotifyTask findByBiz(String type, String bizNo);

    /** CAS 抢占一次尝试，成功返回 true。 */
    boolean claim(Long id, int expectedTimes);

    /** 标记成功并清空下次派发时间，仅覆盖当前次数。 */
    boolean finishSuccess(Long id, int attemptNo, String response, LocalDateTime now);

    /** 标记失败并设置下次派发时间，仅覆盖当前次数。 */
    boolean finishFailed(Long id, int attemptNo, String response, LocalDateTime nextTime, LocalDateTime now);

    /** 追加一次尝试日志。 */
    void appendLog(PayNotifyLog log);

    /** 按任务读取日志，按次数升序。 */
    List<PayNotifyLog> logs(Long taskId);

    /** 分页查询任务。 */
    PageResult<PayNotifyTask> page(PayNotifyTaskFilter filter);

    /** 扫描到期待派发任务。 */
    List<PayNotifyTask> findDue(LocalDateTime now, int limit);

    /** 重置失败任务以便人工重发，成功任务不重置。 */
    boolean resetForRetry(Long id, LocalDateTime now);

    /** 数据库当前时间。 */
    LocalDateTime databaseNow();
}
