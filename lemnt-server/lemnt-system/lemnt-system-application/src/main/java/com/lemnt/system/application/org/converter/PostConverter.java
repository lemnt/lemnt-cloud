package com.lemnt.system.application.org.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.application.org.dto.PostDTO;
import com.lemnt.system.domain.org.model.Post;

/**
 * 岗位 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class PostConverter {
    /**
     * 领域对象转 DTO，保留原始创建时间。
     */
    public static PostDTO toDTO(Post entity) {
        if (entity == null) return null;
        PostDTO dto = new PostDTO();
        dto.setPostId(entity.getPostId());
        dto.setPostCode(entity.getPostCode());
        dto.setPostName(entity.getPostName());
        dto.setPostSort(entity.getPostSort());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    public static List<PostDTO> toDTOList(List<Post> list) {
        if (list == null) return new ArrayList<>();
        List<PostDTO> result = new ArrayList<>(list.size());
        for (Post item : list) result.add(toDTO(item));
        return result;
    }
}
