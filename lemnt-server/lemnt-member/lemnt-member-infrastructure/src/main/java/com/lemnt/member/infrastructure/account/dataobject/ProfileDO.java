package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class ProfileDO {
    private Long id;
    private String memberNo;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private Integer status;
    private Long points;
    private Long experience;
    private Long levelId;
    private Long groupId;
    private Integer sex;
    private java.time.LocalDate birthday;
    private java.time.LocalDateTime createTime;
}
