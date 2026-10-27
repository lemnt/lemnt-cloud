package com.lemnt.system.adapter.web.org.converter;

import com.lemnt.system.adapter.web.org.res.PostRes;
import com.lemnt.system.adapter.web.org.res.SystemPostGetInfoRes;
import com.lemnt.system.adapter.web.org.res.SystemPostListRes;
import com.lemnt.system.application.org.dto.PostDTO;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * PostDTO List → PostInfoRes List
     */
    public static List<SystemPostListRes> toResListToSystemPostListRes(List<PostDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemPostListRes> result = new ArrayList<>(list.size());
        for (PostDTO item : list) {
            result.add(toResToSystemPostListRes(item));
        }
        return result;
    }

    /**
     * PostDTO → PostInfoRes
     */
    public static SystemPostListRes toResToSystemPostListRes(PostDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemPostListRes(
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
     * PostDTO → PostInfoRes
     */
    public static SystemPostGetInfoRes toResToSystemPostGetInfoRes(PostDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemPostGetInfoRes(
                dto.getPostId(),
                dto.getPostCode(),
                dto.getPostName(),
                dto.getPostSort(),
                dto.getStatus(),
                dto.getRemark(),
                dto.getCreateTime()
        );
    }
}
