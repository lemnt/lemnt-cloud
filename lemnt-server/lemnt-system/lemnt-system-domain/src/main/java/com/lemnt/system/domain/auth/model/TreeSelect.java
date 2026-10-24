package com.lemnt.system.domain.auth.model;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.system.domain.org.model.Dept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Treeselect树结构实体类
 *
 * @author wangxx
 */
@Data
public class TreeSelect implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 节点禁用
     */
    private boolean disabled = false;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect() {

    }

    public TreeSelect(Dept dept) {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.disabled = Objects.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public TreeSelect(Menu menu) {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }
}
