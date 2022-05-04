package com.nnte.basebusi.annotation;

public interface PulsarConsumerInterface<T> {
    void onConsumerMessageReceived(T message);
}
