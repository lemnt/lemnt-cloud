package com.lemnt.system.infrastructure.log.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志数据对象 sys_oper_log
 * 严格只含数据库表字段
 *
 * @author wangxx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_oper_log")
public class OperLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 日志主键
     */
    @TableId(type = IdType.AUTO)
    private Long operId;

    /**
     * 操作模块
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    private Integer operatorType;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 主机地址
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（1正常 2异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 消耗时间（毫秒）
     */
    private Long costTime;
}
