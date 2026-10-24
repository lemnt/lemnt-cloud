package com.lemnt.system.infrastructure.config.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.experimental.Accessors;

import com.lemnt.common.infrastructure.domain.BaseDO;

/**
 * 字典类型数据对象 sys_dict_type
 * 严格只含数据库表字段
 *
 * @author wangxx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_dict_type")
public class DictTypeDO extends BaseDO {

    /**
     * 字典主键
     */
    @TableId(type = IdType.AUTO)
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（1正常 2停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
