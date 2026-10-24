package com.lemnt.common.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.lemnt.common.core.constant.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 响应信息主体
 *
 * @author wangxx
 */
@Getter
@Setter
@Schema(description = "响应信息主体")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = Constants.SUCCESS;

    /**
     * 失败
     */
    public static final int FAIL = Constants.FAIL;

    /**
     * 消息状态码
     */
    @Schema(description = "消息状态码")
    private int code;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String msg;

    /**
     * data对象
     */
    @Schema(description = "data对象")
    private T data;

    /**
     * list对象
     */
    @Schema(description = "rows")
    private List<T> rows;

    /**
     * total
     */
    @Schema(description = "total")
    private Long total;

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, null);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, null);
    }

    public static <T> R<T> page(List<T> list, Long total) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setRows(list);
        r.setTotal(total);
        return r;
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> R<T> fail() {
        return restResult(null, FAIL, null);
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> R<T> fail(T data) {
        return restResult(data, FAIL, null);
    }

    public static <T> R<T> fail(T data, String msg) {
        return restResult(data, FAIL, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }
}
