package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class LevelDO {
    private Long id;
    private String name;
    private Integer level;
    private Long experience;
    private Integer discountPercent;
    private String icon;
    private String backgroundUrl;
    private Integer status;
}
