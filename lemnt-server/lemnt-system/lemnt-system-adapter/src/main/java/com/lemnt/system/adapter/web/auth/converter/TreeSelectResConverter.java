package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.res.TreeSelectRes;
import com.lemnt.system.adapter.web.auth.res.UserDeptTreeRes;
import com.lemnt.system.domain.auth.model.TreeSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * 树选择响应对象组装器
 *
 * @author wangxx
 */
public class TreeSelectResConverter {
    /**
     * 仅用户部门入口保留旧领域节点的字段包含规则，其余树接口沿用TreeSelectRes。
     */
    public static List<UserDeptTreeRes> toUserDeptResList(List<TreeSelect> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
            .map(
                tree ->
                    new UserDeptTreeRes(
                        tree.getId(),
                        tree.getLabel(),
                        tree.isDisabled(),
                        toUserDeptResList(tree.getChildren())))
            .toList();
    }

    /**
     * TreeSelect → TreeSelectInfoRes
     */
    public static TreeSelectRes toRes(TreeSelect tree) {
        if (tree == null) {
            return null;
        }
        return new TreeSelectRes(
            tree.getId(), tree.getLabel(), tree.isDisabled(), toResList(tree.getChildren()));
    }

    /**
     * TreeSelect List → TreeSelectInfoRes List
     */
    public static List<TreeSelectRes> toResList(List<TreeSelect> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<TreeSelectRes> result = new ArrayList<>(list.size());
        for (TreeSelect item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
