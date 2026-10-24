package com.lemnt.system.adapter.web.auth.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.auth.res.TreeSelectRes;
import com.lemnt.system.domain.auth.model.TreeSelect;

/**
 * 树选择响应对象组装器
 *
 * @author wangxx
 */
public class TreeSelectResConverter {

    /**
     * TreeSelect → TreeSelectInfoRes
     */
    public static TreeSelectRes toRes(TreeSelect tree) {
        if (tree == null) {
            return null;
        }
        return new TreeSelectRes(
                tree.getId(),
                tree.getLabel(),
                tree.isDisabled(),
                toResList(tree.getChildren())
        );
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
