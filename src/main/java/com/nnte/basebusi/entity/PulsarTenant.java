package com.nnte.basebusi.entity;

import lombok.Data;

import java.util.Set;

@Data
public class PulsarTenant {
    private String tenantCode;
    private Set<String> namespaceSet;
}
