package com.lemnt.system.adapter.web.auth.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 用户部门选项原来直接输出领域节点，保留空名称与空子节点的原JSON规则。
 */
@Schema(description = "用户部门选项节点")
public record UserDeptTreeRes(
    @Schema(description = "部门编号") Long id,
    @Schema(description = "部门名称") String label,
    @Schema(description = "是否禁用") boolean disabled,
    @Schema(description = "下级部门；空集合不输出") @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<UserDeptTreeRes> children)
    implements Serializable {
}
