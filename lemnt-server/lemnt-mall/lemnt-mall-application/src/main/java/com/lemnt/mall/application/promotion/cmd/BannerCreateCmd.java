package com.lemnt.mall.application.promotion.cmd;

import java.io.Serializable;

/** Banner 新增命令。 */
public record BannerCreateCmd(
        String title,
        String url,
        String picUrl,
        Integer sort,
        Integer status,
        Integer position,
        String memo
) implements Serializable {
}
