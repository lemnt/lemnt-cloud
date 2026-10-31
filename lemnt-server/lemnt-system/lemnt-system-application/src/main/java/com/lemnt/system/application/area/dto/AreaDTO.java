package com.lemnt.system.application.area.dto;
import java.util.List;
/** 地区树应用输出。 */
public record AreaDTO(Integer id, String name, List<AreaDTO> children) {}
