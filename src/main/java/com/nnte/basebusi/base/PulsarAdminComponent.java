package com.nnte.basebusi.base;

import com.nnte.basebusi.entity.PulsarTenant;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PulsarAdminComponent {
    private String serviceHttpUrl;
    private Map<String,PulsarTenant> pulsarTenantMap = new HashMap<>();
    /**
     * 添加一个租户和名字空间
     * */
    public void setTenantNamespace(String tenant,String namespace){
        PulsarTenant pt=pulsarTenantMap.get(tenant);
        if (pt==null){
            PulsarTenant newPT = new PulsarTenant();
            newPT.setTenantCode(tenant);
            newPT.setNamespaceSet(new HashSet<>());
            newPT.getNamespaceSet().add(namespace);
            pulsarTenantMap.put(tenant,newPT);
        }else
            pt.getNamespaceSet().add(namespace);
    }
    /**
     *
     * */
    public void initPulsarTenantNamespace(String url) {
        PulsarAdmin admin = null;
        try {
            serviceHttpUrl = url;
            PulsarAdminBuilder adminBuilder = PulsarAdmin.builder();
            admin = adminBuilder.serviceHttpUrl(serviceHttpUrl).build();
            for(PulsarTenant tenant:pulsarTenantMap.values()){
                //---检查是否有租户定义，如果没有则创建---
                TenantInfo tenantInfo = admin.tenants().getTenantInfo(tenant.getTenantCode());
                if (tenantInfo==null){
                    Set<String> allowedClusters = new HashSet<>();
                    allowedClusters.add("pulsar-cluster");
                    TenantInfo info = TenantInfo.builder().allowedClusters(allowedClusters).build();
                    admin.tenants().createTenant(tenant.getTenantCode(),info);
                }
                for(String namespace:tenant.getNamespaceSet()){
                    //检查是否有namsespace定义，没有则创建
                    List<String> list=admin.namespaces().getNamespaces(tenant.getTenantCode());
                    if (list==null || list.size()<1 || list.stream().noneMatch(s->s.split("/")[1].equals(namespace))){
                        admin.namespaces().createNamespace(tenant.getTenantCode()+"/"+namespace);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    public static void main(String[] args){
        PulsarAdminComponent component = new PulsarAdminComponent();
        component.setTenantNamespace("PF-PC-MANAGER","systemBasicInfo");
        component.setTenantNamespace("PF-PC-MANAGER","merchantManage");
        component.initPulsarTenantNamespace("http://39.99.191.134:8080");
    }
}
