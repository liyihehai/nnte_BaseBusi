package com.nnte.basebusi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 系统角色对象
 * */
@Getter
@Setter
@NoArgsConstructor
public class SysRole {
    private String roleCode;
    private String roleName;
    private Map<String,String> rulerMap;
}
