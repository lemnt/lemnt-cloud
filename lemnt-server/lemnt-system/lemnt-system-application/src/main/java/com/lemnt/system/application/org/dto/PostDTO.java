package com.lemnt.system.application.org.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位 DTO
 *
 * @author wangxx
 */
@Data
public class PostDTO {
    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位排序
     */
    private Integer postSort;

    /**
     * 状态（1正常 2停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
