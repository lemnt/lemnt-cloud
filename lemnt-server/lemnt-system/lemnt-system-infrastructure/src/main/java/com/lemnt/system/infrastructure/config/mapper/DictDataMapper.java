package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DictData 数据层
 *
 * @author wangxx
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictDataDO> {
    /**
     * 分页查询字典数据列表
     *
     * @param page     分页参数
     * @param dictData 查询条件
     * @return 分页结果
     */
    IPage<DictDataDO> selectPage(Page<DictDataDO> page, @Param("qry") DictData dictData);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    default List<DictDataDO> selectDictDataByType(String dictType) {
        return this.selectList(Wrappers.<DictDataDO>lambdaQuery()
                .eq(DictDataDO::getDictType, dictType)
                .eq(DictDataDO::getStatus, "1")
                .orderByAsc(DictDataDO::getDictSort));
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    default String selectDictLabel(String dictType, String dictValue) {
        DictDataDO dictData = this.selectOne(Wrappers.<DictDataDO>lambdaQuery()
                .eq(DictDataDO::getDictType, dictType)
                .eq(DictDataDO::getDictValue, dictValue));
        return dictData == null ? "" : dictData.getDictLabel();
    }

    /**
     * 查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    default Long countDictDataByType(String dictType) {
        return this.selectCount(Wrappers.<DictDataDO>lambdaQuery()
                .eq(DictDataDO::getDictType, dictType));
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictType 需要删除的字典类型
     * @return 结果
     */
    default int deleteDictDataByType(String dictType) {
        return this.delete(Wrappers.<DictDataDO>lambdaQuery()
                .eq(DictDataDO::getDictType, dictType));
    }

    /**
     * 同步修改字典类型
     *
     * @param oldDictType 旧字典类型
     * @param newDictType 新旧字典类型
     * @return 结果
     */
    default int updateDictDataType(String oldDictType, String newDictType) {
        return this.update(null, Wrappers.<DictDataDO>lambdaUpdate()
                .eq(DictDataDO::getDictType, oldDictType)
                .set(DictDataDO::getDictType, newDictType));
    }
}
