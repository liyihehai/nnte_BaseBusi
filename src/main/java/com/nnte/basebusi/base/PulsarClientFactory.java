package com.nnte.basebusi.base;

import com.nnte.basebusi.entity.PulsarClientEntity;
import org.apache.commons.collections.map.HashedMap;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class PulsarClientFactory extends BaseComponent{
    private final static Map<String, PulsarClientEntity> pulsarClientMap = new HashedMap();
    public synchronized PulsarClient getPulsarClient(String ip,String port) throws PulsarClientException {
        String key = ip+":"+port;
        PulsarClientEntity client=pulsarClientMap.get(key);
        if (client!=null)
            return client.getClient();
        String serviceUrl = "pulsar://" + ip + ":" + port;
        PulsarClient pulsarClient = PulsarClient.builder()
                                    .serviceUrl(serviceUrl)
                                    .build();
        outLogInfo("PulsarClient serviceUrl="+serviceUrl);
        pulsarClientMap.put(key,new PulsarClientEntity(ip,port,pulsarClient));
        return pulsarClient;
    }
}
