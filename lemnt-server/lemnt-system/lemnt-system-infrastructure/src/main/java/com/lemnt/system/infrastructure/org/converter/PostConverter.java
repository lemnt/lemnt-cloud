package com.lemnt.system.infrastructure.org.converter;

import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.infrastructure.org.dataobject.PostDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 岗位 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class PostConverter {

    /**
     * DO → Entity
     */
    public static Post toEntity(PostDO src) {
        if (src == null) return null;
        Post e = new Post();
        e.setPostId(src.getPostId());
        e.setPostCode(src.getPostCode());
        e.setPostName(src.getPostName());
        e.setPostSort(src.getPostSort());
        e.setStatus(src.getStatus());
        e.setRemark(src.getRemark());
        e.setCreateBy(src.getCreateBy());
        e.setCreateTime(src.getCreateTime());
        e.setUpdateBy(src.getUpdateBy());
        e.setUpdateTime(src.getUpdateTime());
        return e;
    }

    /**
     * Entity → DO
     */
    public static PostDO toDO(Post src) {
        if (src == null) return null;
        PostDO d = new PostDO();
        d.setPostId(src.getPostId());
        d.setPostCode(src.getPostCode());
        d.setPostName(src.getPostName());
        d.setPostSort(src.getPostSort());
        d.setStatus(src.getStatus());
        d.setRemark(src.getRemark());
        d.setCreateBy(src.getCreateBy());
        d.setCreateTime(src.getCreateTime());
        d.setUpdateBy(src.getUpdateBy());
        d.setUpdateTime(src.getUpdateTime());
        return d;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Post> toEntityList(List<PostDO> list) {
        if (list == null) return new ArrayList<>();
        List<Post> result = new ArrayList<>(list.size());
        for (PostDO item : list) result.add(toEntity(item));
        return result;
    }
}
