package com.lemnt.mall.adapter.web.product.res;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 只导出当前真实商品字段，编号以文本保存，避免Excel丢失大编号精度。 */
@Data
@AllArgsConstructor
@ColumnWidth(24)
public class ProductSpuExportRes {
    @ExcelProperty(value = "商品编号", index = 0)
    private String id;
    @ExcelProperty(value = "商品编码", index = 1)
    private String code;
    @ExcelProperty(value = "商品名称", index = 2)
    private String name;
    @ExcelProperty(value = "分类编号", index = 3)
    private String categoryId;
    @ExcelProperty(value = "品牌编号", index = 4)
    private String brandId;
    @ExcelProperty(value = "状态", index = 5)
    private String status;
    @ExcelProperty(value = "商品描述", index = 6)
    private String description;
    @ExcelProperty(value = "关键字", index = 7)
    private String keyword;
    @ExcelProperty(value = "简介", index = 8)
    private String introduction;
    @ExcelProperty(value = "轮播图", index = 9)
    private String sliderPicUrls;
    @ExcelProperty(value = "排序", index = 10)
    private Integer sort;
}
