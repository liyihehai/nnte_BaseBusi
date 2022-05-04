package com.nnte.basebusi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.pulsar.client.api.PulsarClient;

@Data
@AllArgsConstructor
public class PulsarClientEntity {
    private String ip;
    private String port;
    private PulsarClient client;
}
