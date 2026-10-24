package com.lemnt.system.infrastructure.auth.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;

/**
 * 用户对象转换器
 *
 * @author wangxx
 */
public class UserConverter {

    /**
     * DO → Entity
     */
    public static User toEntity(UserDO do0) {
        if (do0 == null) return null;
        User entity = new User();
        entity.setUserId(do0.getUserId());
        entity.setDeptId(do0.getDeptId());
        entity.setUserName(do0.getUserName());
        entity.setNickName(do0.getNickName());
        entity.setEmail(do0.getEmail());
        entity.setPhonenumber(do0.getPhonenumber());
        entity.setSex(do0.getSex());
        entity.setAvatar(do0.getAvatar());
        entity.setPassword(do0.getPassword());
        entity.setStatus(do0.getStatus());
        entity.setDelFlag(do0.getDelFlag());
        entity.setLoginIp(do0.getLoginIp());
        entity.setLoginDate(do0.getLoginDate());
        entity.setPwdUpdateDate(do0.getPwdUpdateDate());
        entity.setCreateBy(do0.getCreateBy());
        entity.setUpdateBy(do0.getUpdateBy());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static UserDO toDO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO item = new UserDO();
        item.setUserId(entity.getUserId());
        item.setDeptId(entity.getDeptId());
        item.setUserName(entity.getUserName());
        item.setNickName(entity.getNickName());
        item.setEmail(entity.getEmail());
        item.setPhonenumber(entity.getPhonenumber());
        item.setSex(entity.getSex());
        item.setAvatar(entity.getAvatar());
        item.setPassword(entity.getPassword());
        item.setStatus(entity.getStatus());
        item.setDelFlag(entity.getDelFlag());
        item.setLoginIp(entity.getLoginIp());
        item.setLoginDate(entity.getLoginDate());
        item.setPwdUpdateDate(entity.getPwdUpdateDate());
        item.setCreateBy(entity.getCreateBy());
        item.setUpdateBy(entity.getUpdateBy());
        return item;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<User> toEntityList(List<UserDO> doList) {
        if (doList == null) {
            return null;
        }
        return doList.stream().map(UserConverter::toEntity).collect(Collectors.toList());
    }
}
