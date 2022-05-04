package com.nnte.basebusi.base;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerInterceptor;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;

import java.util.Set;

public class ConsumerInterceptorlmpl<T> implements ConsumerInterceptor<T> {
    @Override
    public void close() {
        System.out.println("ConsumerInterceptorlmpl close");
    }

    @Override
    public Message beforeConsume(Consumer consumer, Message message) {
        System.out.println("beforeConsume message="+message.getMessageId());
        return message;
    }

    @Override
    public void onAcknowledge(Consumer consumer, MessageId messageId, Throwable exception) {
        System.out.println("onAcknowledge MessageId="+messageId);
    }

    @Override
    public void onAcknowledgeCumulative(Consumer consumer, MessageId messageId, Throwable exception) {
        System.out.println("onAcknowledgeCumulative MessageId="+messageId);
    }

    @Override
    public void onAckTimeoutSend(Consumer consumer, Set set) {
        System.out.println("onAckTimeoutSend consumer="+consumer.getConsumerName());
    }

    @Override
    public void onNegativeAcksSend(Consumer consumer, Set set) {
        System.out.println("onNegativeAcksSend consumer="+consumer.getConsumerName());
    }
}
