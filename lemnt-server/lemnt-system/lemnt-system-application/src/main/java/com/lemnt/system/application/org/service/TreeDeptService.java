package com.lemnt.system.application.org.service;

import com.lemnt.system.domain.auth.model.TreeSelect;
import com.lemnt.system.domain.org.model.Dept;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TreeDeptService {
    /**
     * 构建前端所需要下拉树结构
     *
     * @param deptList 部门列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildTreeSelect(List<Dept> deptList) {
        List<Dept> deptTrees = buildDeptTree(deptList);
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要树结构
     *
     * @param deptList 部门列表
     * @return 树结构列表
     */
    public List<Dept> buildDeptTree(List<Dept> deptList) {
        List<Dept> returnList = new ArrayList<>();
        List<Long> tempList = deptList.stream().map(Dept::getDeptId).toList();
        for (Dept dept : deptList) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(deptList, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = deptList;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<Dept> list, Dept t) {
        // 得到子节点列表
        List<Dept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Dept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<Dept> getChildList(List<Dept> list, Dept t) {
        List<Dept> tlist = new ArrayList<>();
        for (Dept n : list) {
            if (Objects.nonNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<Dept> list, Dept t) {
        return !getChildList(list, t).isEmpty();
    }
}
