package com.lemnt.mall.application.promotion.cmd;

import java.io.Serializable;

/** Banner 修改命令。 */
public record BannerUpdateCmd(
        Long id,
        String title,
        String url,
        String picUrl,
        Integer sort,
        Integer status,
        Integer position,
        String memo
) implements Serializable {
}
