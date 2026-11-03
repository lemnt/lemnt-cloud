package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.DiyPageSaveCmd;
import com.lemnt.mall.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.mall.application.promotion.cmd.DiyTemplateSaveCmd;
import com.lemnt.mall.application.promotion.dto.AppDiyPageDTO;
import com.lemnt.mall.application.promotion.dto.AppDiyTemplateDTO;
import com.lemnt.mall.application.promotion.dto.DiyPageDTO;
import com.lemnt.mall.application.promotion.dto.DiyTemplateDTO;
import com.lemnt.mall.application.promotion.qry.DiyPagePageQry;
import com.lemnt.mall.application.promotion.qry.DiyTemplatePageQry;
import com.lemnt.mall.domain.promotion.gateway.DiyPageGateway;
import com.lemnt.mall.domain.promotion.gateway.DiyTemplateGateway;
import com.lemnt.mall.domain.promotion.model.DiyPage;
import com.lemnt.mall.domain.promotion.model.DiyPageFilter;
import com.lemnt.mall.domain.promotion.model.DiyTemplate;
import com.lemnt.mall.domain.promotion.model.DiyTemplateFilter;

import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 装修模板与页面编排：新建模板自动生成首页和我的页面，属性只做 JSON 结构校验。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DiyDecorationService {
    private static final String INDEX_PAGE = "首页";
    private static final String MY_PAGE = "我的";
    private static final String EMPTY_PROPERTY = "{}";

    private final DiyTemplateGateway templates;
    private final DiyPageGateway pages;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private DiyTemplate template(Long id) {
        var value = templates.find(id);
        require(value != null, "装修模板不存在");
        return value;
    }

    private DiyPage page(Long id) {
        var value = pages.find(id);
        require(value != null, "装修页面不存在");
        return value;
    }

    private String name(String value) {
        require(value != null && !value.isBlank() && value.length() <= 100, "名称不能为空且不超过100字");
        return value.trim();
    }

    private List<String> previews(List<String> values) {
        if (values == null) {
            return List.of();
        }
        require(values.size() <= 9, "预览图最多9张");
        require(
                values.stream()
                        .allMatch(item -> item != null && !item.isBlank() && item.length() <= 500),
                "预览图地址不合法");
        return values.stream().map(String::trim).toList();
    }

    private String property(String value) {
        require(value != null && !value.isBlank() && value.length() <= 60000, "装修属性不能为空且不超过60000字");
        try {
            var parsed = JSON.parse(value);
            require(
                    parsed instanceof Map || parsed instanceof List,
                    "装修属性必须是JSON对象或数组");
            return value;
        } catch (RuntimeException exception) {
            throw new ServiceException("装修属性必须是合法JSON", 400);
        }
    }

    private void uniqueName(Long id, String name) {
        var existing = templates.findByName(name);
        require(existing == null || existing.id().equals(id), "装修模板名称已存在");
    }

    public Long createTemplate(DiyTemplateSaveCmd cmd) {
        require(cmd != null && cmd.id() == null, "装修模板参数不合法");
        String name = name(cmd.name());
        uniqueName(null, name);
        var saved =
                templates.save(
                        new DiyTemplate(
                                null,
                                name,
                                false,
                                null,
                                cmd.remark(),
                                previews(cmd.previewPicUrls()),
                                EMPTY_PROPERTY));
        createDefaultPage(saved, INDEX_PAGE);
        createDefaultPage(saved, MY_PAGE);
        return saved.id();
    }

    private void createDefaultPage(DiyTemplate template, String name) {
        pages.save(
                new DiyPage(
                        null,
                        template.id(),
                        name,
                        "模板【" + template.name() + "】自动创建",
                        List.of(),
                        EMPTY_PROPERTY));
    }

    public Boolean updateTemplate(DiyTemplateSaveCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "装修模板编号不合法");
        var existing = template(cmd.id());
        String name = name(cmd.name());
        uniqueName(cmd.id(), name);
        templates.save(
                new DiyTemplate(
                        existing.id(),
                        name,
                        existing.used(),
                        existing.usedTime(),
                        cmd.remark(),
                        previews(cmd.previewPicUrls()),
                        existing.property()));
        return true;
    }

    /** 切换启用模板：先取消原模板，再启用目标模板。 */
    public Boolean useTemplate(Long id) {
        require(id != null && id > 0, "装修模板编号不合法");
        template(id);
        var used = templates.findUsed();
        if (used != null && used.id().equals(id)) {
            return true;
        }
        if (used != null) {
            templates.save(
                    new DiyTemplate(
                            used.id(),
                            used.name(),
                            false,
                            null,
                            used.remark(),
                            used.previewPicUrls(),
                            used.property()));
        }
        var target = template(id);
        templates.save(
                new DiyTemplate(
                        target.id(),
                        target.name(),
                        true,
                        LocalDateTime.now(),
                        target.remark(),
                        target.previewPicUrls(),
                        target.property()));
        return true;
    }

    public Boolean deleteTemplate(Long id) {
        require(id != null && id > 0, "装修模板编号不合法");
        var existing = template(id);
        require(!Boolean.TRUE.equals(existing.used()), "使用中的装修模板不能删除");
        templates.delete(id);
        return true;
    }

    public DiyTemplateDTO templateDetail(Long id) {
        return templateDto(template(id));
    }

    public PageResult<DiyTemplateDTO> templatePage(DiyTemplatePageQry qry) {
        requirePage(qry == null ? 0 : qry.pageNum(), qry == null ? 0 : qry.pageSize());
        var result =
                templates.page(
                        new DiyTemplateFilter(
                                qry.name(), qry.used(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(
                result.getRecords().stream().map(this::templateDto).toList(), result.getTotal());
    }

    public Boolean updateTemplateProperty(DiyPropertyCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "装修模板编号不合法");
        var existing = template(cmd.id());
        templates.save(
                new DiyTemplate(
                        existing.id(),
                        existing.name(),
                        existing.used(),
                        existing.usedTime(),
                        existing.remark(),
                        existing.previewPicUrls(),
                        property(cmd.property())));
        return true;
    }

    public Long createPage(DiyPageSaveCmd cmd) {
        require(cmd != null && cmd.id() == null, "装修页面参数不合法");
        var template = template(cmd.templateId());
        String name = name(cmd.name());
        require(pages.findByName(template.id(), name) == null, "模板下页面名称已存在");
        return pages.save(
                        new DiyPage(
                                null,
                                template.id(),
                                name,
                                cmd.remark(),
                                previews(cmd.previewPicUrls()),
                                EMPTY_PROPERTY))
                .id();
    }

    public Boolean updatePage(DiyPageSaveCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "装修页面编号不合法");
        var existing = page(cmd.id());
        var template = template(cmd.templateId());
        String name = name(cmd.name());
        var same = pages.findByName(template.id(), name);
        require(same == null || same.id().equals(cmd.id()), "模板下页面名称已存在");
        pages.save(
                new DiyPage(
                        existing.id(),
                        template.id(),
                        name,
                        cmd.remark(),
                        previews(cmd.previewPicUrls()),
                        existing.property()));
        return true;
    }

    public Boolean deletePage(Long id) {
        require(id != null && id > 0, "装修页面编号不合法");
        page(id);
        pages.delete(id);
        return true;
    }

    public DiyPageDTO pageDetail(Long id) {
        return pageDto(page(id));
    }

    public PageResult<DiyPageDTO> pagePage(DiyPagePageQry qry) {
        requirePage(qry == null ? 0 : qry.pageNum(), qry == null ? 0 : qry.pageSize());
        var result =
                pages.page(
                        new DiyPageFilter(
                                qry.templateId(), qry.name(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(
                result.getRecords().stream().map(this::pageDto).toList(), result.getTotal());
    }

    public Boolean updatePageProperty(DiyPropertyCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "装修页面编号不合法");
        var existing = page(cmd.id());
        pages.save(
                new DiyPage(
                        existing.id(),
                        existing.templateId(),
                        existing.name(),
                        existing.remark(),
                        existing.previewPicUrls(),
                        property(cmd.property())));
        return true;
    }

    public AppDiyTemplateDTO usedTemplate() {
        var template = templates.findUsed();
        if (template == null) {
            return null;
        }
        var pages = this.pages.byTemplateId(template.id());
        return appTemplate(template, pages);
    }

    public AppDiyTemplateDTO appTemplate(Long id) {
        var template = template(id);
        return appTemplate(template, pages.byTemplateId(template.id()));
    }

    public AppDiyPageDTO appPage(Long id) {
        var page = page(id);
        return new AppDiyPageDTO(page.id(), page.name(), page.property());
    }

    private AppDiyTemplateDTO appTemplate(DiyTemplate template, List<DiyPage> pages) {
        String home =
                pages.stream()
                        .filter(page -> INDEX_PAGE.equals(page.name()))
                        .map(DiyPage::property)
                        .findFirst()
                        .orElse(null);
        String user =
                pages.stream()
                        .filter(page -> MY_PAGE.equals(page.name()))
                        .map(DiyPage::property)
                        .findFirst()
                        .orElse(null);
        return new AppDiyTemplateDTO(
                template.id(), template.name(), template.property(), home, user);
    }

    private DiyTemplateDTO templateDto(DiyTemplate value) {
        return new DiyTemplateDTO(
                value.id(),
                value.name(),
                value.used(),
                value.usedTime(),
                value.remark(),
                value.previewPicUrls(),
                value.property());
    }

    private DiyPageDTO pageDto(DiyPage value) {
        return new DiyPageDTO(
                value.id(),
                value.templateId(),
                value.name(),
                value.remark(),
                value.previewPicUrls(),
                value.property());
    }

    private void requirePage(int pageNum, int pageSize) {
        require(pageNum >= 1 && pageNum <= 1000000 && pageSize >= 1 && pageSize <= 100, "分页参数不合法");
    }
}
