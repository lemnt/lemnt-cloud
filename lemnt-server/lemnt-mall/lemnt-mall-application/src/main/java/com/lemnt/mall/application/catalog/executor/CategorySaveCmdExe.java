package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.CategorySaveCmd;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.CategoryDTO;
import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.Category;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 分类保存命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CategorySaveCmdExe {
    private final CategoryGateway categoryGateway;

    /**
     * 执行分类保存命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryDTO execute(CategorySaveCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("分类参数不能为空");
        }
        if (cmd.id() != null) {
            CatalogRules.id(cmd.id());
            var old = categoryGateway.category(cmd.id());
            if (old == null) {
                throw new IllegalArgumentException("分类不存在");
            }
            if (!Objects.equals(old.parentId(), cmd.parentId())) {
                throw new IllegalArgumentException("分类父级不允许修改");
            }
        }
        if (cmd.parentId() != null) {
            CatalogRules.id(cmd.parentId());
            var parent = categoryGateway.category(cmd.parentId());
            if (parent == null || parent.parentId() != null || cmd.parentId().equals(cmd.id())) {
                throw new IllegalArgumentException("分类只允许两级且父分类必须存在");
            }
        }
        if (cmd.sort() == null || cmd.sort() < 0) {
            throw new IllegalArgumentException("排序不能小于0");
        }
        var category =
                new Category(
                cmd.id(),
                cmd.parentId(),
                CatalogRules.text(cmd.name(), 100, "分类名称"),
                CatalogRules.optional(cmd.picUrl(), 500),
                cmd.sort(),
                CatalogRules.status(cmd.status()));
        return CatalogDTOConverter.category(categoryGateway.saveCategory(category));
    }
}
