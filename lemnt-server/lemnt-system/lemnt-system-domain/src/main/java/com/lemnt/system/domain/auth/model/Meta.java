package com.lemnt.system.domain.auth.model;

import com.lemnt.common.core.utils.StringUtils;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 路由显示信息
 *
 * @author wangxx
 */
@Data
public class Meta implements Serializable {
    @Serial
    private static final long serialVersionUID = 5294520182945761933L;

    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private boolean noCache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;
    public Meta() {
    }

    public Meta(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public Meta(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    public Meta(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }

    public Meta(String title, String icon, boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (StringUtils.ishttp(link)) {
            this.link = link;
        }
    }
}
