package com.lemnt.system.adapter.web.org.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.org.res.PostRes;
import com.lemnt.system.application.org.dto.PostDTO;

/**
 * 岗位响应对象组装器
 *
 * @author wangxx
 */
public class PostResConverter {

    /**
     * PostDTO → PostInfoRes
     */
    public static PostRes toRes(PostDTO dto) {
        if (dto == null) {
            return null;
        }
        return new PostRes(
                dto.getPostId(),
                dto.getPostCode(),
                dto.getPostName(),
                dto.getPostSort(),
                dto.getStatus(),
                dto.getRemark(),
                dto.getCreateTime()
        );
    }

    /**
     * PostDTO List → PostInfoRes List
     */
    public static List<PostRes> toResList(List<PostDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<PostRes> result = new ArrayList<>(list.size());
        for (PostDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
