package com.lemnt.mall.application.catalog.cmd;

public record CategorySaveCmd(
        Long id, Long parentId, String name, String picUrl, Integer sort, String status) {}
