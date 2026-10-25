package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategorySaveCmdExe {
    private final CatalogGateway gateway;

    @Transactional(rollbackFor = Exception.class)
    public CategoryDTO execute(CategorySaveCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("分类参数不能为空");
        if (cmd.id() != null) {
            CatalogRules.id(cmd.id());
            var old = gateway.category(cmd.id());
            if (old == null) throw new IllegalArgumentException("分类不存在");
            if (!java.util.Objects.equals(old.parentId(), cmd.parentId())) {
                throw new IllegalArgumentException("分类父级不允许修改");
            }
        }
        if (cmd.parentId() != null) {
            CatalogRules.id(cmd.parentId());
            var parent = gateway.category(cmd.parentId());
            if (parent == null || parent.parentId() != null || cmd.parentId().equals(cmd.id())) {
                throw new IllegalArgumentException("分类只允许两级且父分类必须存在");
            }
        }
        if (cmd.sort() == null || cmd.sort() < 0) throw new IllegalArgumentException("排序不能小于0");
        var category =
                new Category(
                        cmd.id(),
                        cmd.parentId(),
                        CatalogRules.text(cmd.name(), 100, "分类名称"),
                        CatalogRules.optional(cmd.picUrl(), 500),
                        cmd.sort(),
                        CatalogRules.status(cmd.status()));
        return CatalogDTOConverter.category(gateway.saveCategory(category));
    }
}
