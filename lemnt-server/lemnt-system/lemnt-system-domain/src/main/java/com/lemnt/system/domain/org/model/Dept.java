package com.lemnt.system.domain.org.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门聚合根实体
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class Dept implements Serializable {
    @Serial
    private static final long serialVersionUID = -8718952977066491257L;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态（1正常 2停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 父部门名称（查询用，非DB字段）
     */
    private String parentName;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 数据范围（1：所有数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    private String dataScope;

    /**
     * 子部门列表
     */
    private List<Dept> children = new ArrayList<>();

    /**
     * 构建部门树结构
     */
    public static List<Dept> buildDeptTree(List<Dept> depts) {
        List<Dept> returnList = new ArrayList<>();
        List<Long> tempList = depts.stream().map(Dept::getDeptId).collect(Collectors.toList());
        for (Dept dept : depts) {
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    private static void recursionFn(List<Dept> list, Dept t) {
        List<Dept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Dept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    private static List<Dept> getChildList(List<Dept> list, Dept t) {
        List<Dept> tlist = new ArrayList<>();
        Iterator<Dept> it = list.iterator();
        while (it.hasNext()) {
            Dept n = it.next();
            if (n.getParentId() != null && n.getParentId().longValue() == t.getDeptId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private static boolean hasChild(List<Dept> list, Dept t) {
        return getChildList(list, t).size() > 0;
    }
}
