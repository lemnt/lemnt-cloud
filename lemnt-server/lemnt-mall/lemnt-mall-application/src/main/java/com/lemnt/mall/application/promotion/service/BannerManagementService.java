package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.BannerCreateCmd;
import com.lemnt.mall.application.promotion.cmd.BannerUpdateCmd;
import com.lemnt.mall.application.promotion.dto.AppBannerDTO;
import com.lemnt.mall.application.promotion.dto.BannerDTO;
import com.lemnt.mall.application.promotion.qry.BannerPageQry;
import com.lemnt.mall.domain.promotion.gateway.BannerGateway;
import com.lemnt.mall.domain.promotion.model.Banner;
import com.lemnt.mall.domain.promotion.model.BannerFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/** Banner 管理：位置与状态校验，点击量由 App 单独累加。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BannerManagementService {
    private static final Set<Integer> POSITIONS = Set.of(1, 2, 3, 4, 5);

    private final BannerGateway banners;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private Banner banner(Long id) {
        var value = banners.find(id);
        require(value != null, "Banner 不存在");
        return value;
    }

    private void validate(String title, String url, String picUrl, Integer sort, Integer status,
            Integer position, String memo) {
        require(title != null && !title.isBlank() && title.length() <= 100, "标题不能为空且不超过100字");
        require(url != null && !url.isBlank() && url.length() <= 500, "跳转链接不能为空且不超过500字");
        require(picUrl != null && !picUrl.isBlank() && picUrl.length() <= 500, "图片地址不能为空且不超过500字");
        require(sort != null, "排序不能为空");
        require(status != null && (status == 0 || status == 1), "状态只能为0开启或1关闭");
        require(position != null && POSITIONS.contains(position), "Banner 位置不合法");
        require(memo == null || memo.length() <= 255, "备注不超过255字");
    }

    public Long create(BannerCreateCmd cmd) {
        require(cmd != null, "Banner 参数不能为空");
        validate(cmd.title(), cmd.url(), cmd.picUrl(), cmd.sort(), cmd.status(), cmd.position(), cmd.memo());
        return banners.save(new Banner(null, cmd.title().trim(), cmd.url().trim(), cmd.picUrl().trim(),
                cmd.sort(), cmd.status(), cmd.position(), cmd.memo(), 0)).id();
    }

    /** 修改时保留点击量，不能被管理端覆盖。 */
    public Boolean update(BannerUpdateCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "Banner 编号不合法");
        var existing = banner(cmd.id());
        validate(cmd.title(), cmd.url(), cmd.picUrl(), cmd.sort(), cmd.status(), cmd.position(), cmd.memo());
        banners.save(new Banner(existing.id(), cmd.title().trim(), cmd.url().trim(), cmd.picUrl().trim(),
                cmd.sort(), cmd.status(), cmd.position(), cmd.memo(), existing.browseCount()));
        return true;
    }

    public Boolean delete(Long id) {
        require(id != null && id > 0, "Banner 编号不合法");
        banner(id);
        banners.delete(id);
        return true;
    }

    public BannerDTO detail(Long id) {
        require(id != null && id > 0, "Banner 编号不合法");
        return dto(banner(id));
    }

    public PageResult<BannerDTO> page(BannerPageQry qry) {
        require(qry != null && qry.pageNum() >= 1 && qry.pageNum() <= 1000000
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        require(qry.status() == null || qry.status() == 0 || qry.status() == 1, "状态只能为0或1");
        var result = banners.page(new BannerFilter(qry.title(), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(result.getRecords().stream().map(this::dto).toList(), result.getTotal());
    }

    public List<AppBannerDTO> appList(Integer position) {
        require(position != null && POSITIONS.contains(position), "Banner 位置不合法");
        return banners.enabledByPosition(position).stream()
                .map(value -> new AppBannerDTO(value.id(), value.title(), value.url(), value.picUrl()))
                .toList();
    }

    public Boolean addBrowseCount(Long id) {
        require(id != null && id > 0, "Banner 编号不合法");
        banner(id);
        require(banners.increaseBrowseCount(id), "Banner 点击量更新失败");
        return true;
    }

    private BannerDTO dto(Banner value) {
        return new BannerDTO(value.id(), value.title(), value.url(), value.picUrl(), value.sort(),
                value.status(), value.position(), value.memo(), value.browseCount());
    }
}
