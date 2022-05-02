package com.nnte.basebusi.base;

import lombok.Setter;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.schema.JSONSchema;

import java.util.concurrent.*;

public abstract class PulsarComponent<T> extends BaseComponent{

    @Setter
    private Class<T> contentClazz;
    private PulsarClient PULSAR_CLIENT=null;
    private Producer<T> producer;
    private Consumer<T> consumer;
    private Executor consumerExecutor;

    private void reConnectPulsarServer(String serviceUrl) throws PulsarClientException{
        PULSAR_CLIENT = PulsarClient.builder()
                .serviceUrl(serviceUrl)
                .build();
        outLogInfo("PulsarClient serviceUrl="+serviceUrl);
    }
    public void initPulsarClient(String pulsarIP,String pulsarPort) throws PulsarClientException{
        String serviceUrl = "pulsar://" + pulsarIP + ":" + pulsarPort;
        // 创建pulsar客户端
        if (PULSAR_CLIENT==null) {
            reConnectPulsarServer(serviceUrl);
            return;
        }
        if (PULSAR_CLIENT.isClosed()){
            outLogInfo("PulsarClient serviceUrl="+serviceUrl+",Client is closed,Try to reConnect");
            reConnectPulsarServer(serviceUrl);
        }
    }
    /**
     * 创建生产者
     * */
    public void createProducer(String topic) throws PulsarClientException{
        ProducerBuilder<T> pb=PULSAR_CLIENT.newProducer(JSONSchema.of(contentClazz));
        producer = pb.topic(topic).create();
    }
    /**
     * 生产者发送消息
     */
    public CompletableFuture<MessageId> sendAsyncMessage(T content) throws PulsarClientException{
        if (!producer.isConnected())
            throw new PulsarClientException("pulsar product is no connect server!");
        return producer.sendAsync(content);
    }
    /**
     * 创建消费者，同时创建了处理的线程池，需要设置
     * consumnerThreadCount：线程池大小
     * blockSize：缓存队列大小
     * */
    public void createCustmou(String topic,String subscriptionName,int consumnerThreadCount,int blockSize) throws PulsarClientException{
        ConsumerBuilder<T> pb=PULSAR_CLIENT.newConsumer(JSONSchema.of(contentClazz));
        consumerExecutor = new ThreadPoolExecutor(consumnerThreadCount, consumnerThreadCount, consumnerThreadCount,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockSize));

        consumer = pb.topic(topic).subscriptionName(subscriptionName)
                                  .ackTimeout(10,TimeUnit.SECONDS)
                                  .messageListener((MessageListener<T>) (comsumer,msg)->{
                                      consumerExecutor.execute(() -> handleMsg(comsumer,msg));
                                  })
                                  .subscribe();
    }

    /**
     * 线程池异步处理
     * @param consumer 消费者
     * @param msg 消息
     */
    private void handleMsg(Consumer consumer, Message<T> msg){
        try {
            consumer.acknowledge(msg);
            onConsumerMessageReceived(msg.getValue());
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }
    /**
     * 本函数需要被重载
     * */
    public abstract void onConsumerMessageReceived(T message);
}
