package com.lemnt.product.adapter.web.product.res;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 只导出当前真实商品字段，编号以文本保存，避免Excel丢失大编号精度。 */
@Data
@AllArgsConstructor
@ColumnWidth(24)
@Schema(description = "商品导出行")
public class ProductSpuExportRes {
    @Schema(description = "商品编号")
    @ExcelProperty(value = "商品编号", index = 0)
    private String id;

    @Schema(description = "商品编码")
    @ExcelProperty(value = "商品编码", index = 1)
    private String code;

    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称", index = 2)
    private String name;

    @Schema(description = "分类编号")
    @ExcelProperty(value = "分类编号", index = 3)
    private String categoryId;

    @Schema(description = "品牌编号")
    @ExcelProperty(value = "品牌编号", index = 4)
    private String brandId;

    @Schema(description = "状态")
    @ExcelProperty(value = "状态", index = 5)
    private String status;

    @Schema(description = "商品描述")
    @ExcelProperty(value = "商品描述", index = 6)
    private String description;

    @Schema(description = "关键字")
    @ExcelProperty(value = "关键字", index = 7)
    private String keyword;

    @Schema(description = "简介")
    @ExcelProperty(value = "简介", index = 8)
    private String introduction;

    @Schema(description = "轮播图")
    @ExcelProperty(value = "轮播图", index = 9)
    private String sliderPicUrls;

    @Schema(description = "排序")
    @ExcelProperty(value = "排序", index = 10)
    private Integer sort;
}
