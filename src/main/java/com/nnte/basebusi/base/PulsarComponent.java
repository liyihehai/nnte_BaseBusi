package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.PulsarConsumerInterface;
import lombok.Setter;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

public abstract class PulsarComponent<T> extends BaseComponent implements PulsarConsumerInterface<T> {

    @Autowired
    private PulsarClientFactory pulsarClientFactory;
    private PulsarClient PULSAR_CLIENT = null;

    @Setter
    private Class<T> contentClazz;
    private Producer<T> producer;
    private PulsarClientConsumer<T> clientConsumer = new PulsarClientConsumer<>();
    private ConsumerInterceptorlmpl<T> lmpl = new ConsumerInterceptorlmpl<>();

    public void initPulsarClient(String pulsarIP,String pulsarPort) throws PulsarClientException{
        PULSAR_CLIENT = pulsarClientFactory.getPulsarClient(pulsarIP,pulsarPort);
    }
    private String makeTopicString(boolean isPersistent,String tenant,String namespace,String topic){
        StringBuilder sb=new StringBuilder();
        if (isPersistent)
            sb.append("persistent");
        else
            sb.append("non-persistent");
        sb.append("://");
        sb.append(tenant).append("/").append(namespace).append("/").append(topic);
        return sb.toString();
    }
    /**
     * 创建共享型生产者
     * */
    public void createProducer(boolean isPersistent,String tenant,String namespace,
                               String topic,ProducerAccessMode accessMode) throws PulsarClientException{
        ProducerBuilder<T> pb=PULSAR_CLIENT.newProducer(JSONSchema.of(contentClazz));
        String mTopic=makeTopicString(isPersistent,tenant,namespace,topic);
        producer = pb.topic(mTopic)
                     .accessMode(accessMode)
                     .enableBatching(true)
                     .compressionType(CompressionType.LZ4)
                     .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
                     .sendTimeout(0, TimeUnit.SECONDS)
                     .batchingMaxMessages(1000)
                     .maxPendingMessages(1000)
                     .blockIfQueueFull(true)
                     .roundRobinRouterBatchingPartitionSwitchFrequency(10)
                     .batcherBuilder(BatcherBuilder.DEFAULT)
                     .create();
    }
    /**
     * 生产者发送异步消息（如果要取得结果需要future.join()）
     */
    public CompletableFuture<MessageId> sendAsyncMessage(T content) throws PulsarClientException{
        if (!producer.isConnected())
            throw new PulsarClientException("pulsar product is no connect server!");
        CompletableFuture<MessageId> future=producer.sendAsync(content);
        return future;
    }
    /**
     * 生产者发送同步消息
     */
    public MessageId sendSyncMessage(T content) throws PulsarClientException{
        if (!producer.isConnected())
            throw new PulsarClientException("pulsar product is no connect server!");
        MessageId messageId=producer.send(content);
        return messageId;
    }

    /**
     * 创建消费者，同时创建了处理的线程池，需要设置
     * consumnerThreadCount：线程池大小
     * blockSize：缓存队列大小
     * */
    public void createCustmou(boolean isPersistent,String tenant,String namespace,
                              String topic,String ip,String consumerName,SubscriptionType subscriptionType,
                              int consumerThreadCount,int blockSize) throws PulsarClientException{
        ConsumerBuilder<T> pb=PULSAR_CLIENT.newConsumer(JSONSchema.of(contentClazz));
        String mTopic=makeTopicString(isPersistent,tenant,namespace,topic);

        ConsumerInterceptor<T>[] consumerInterceptors = new ConsumerInterceptor[]{lmpl};
        Consumer<T> consumer = pb.consumerName(topic+"-"+ip)
                     .topic(mTopic)
                     .subscriptionName(consumerName)
                     .subscriptionType(subscriptionType)
                     .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)//指定从哪里开始消费，还有Latest，valueof可选，默认Latest
                     .negativeAckRedeliveryDelay(60, TimeUnit.SECONDS)//指定消费失败后延迟多久broker重新发送消息给consumer，默认60s
                     .acknowledgmentGroupTime(0,TimeUnit.SECONDS)
                     .enableBatchIndexAcknowledgment(true)
                     .enableRetry(true)
                     .intercept(consumerInterceptors)
                     .subscribe();
        clientConsumer.startConsumer(consumer,true,this,consumerThreadCount,blockSize);
    }
}
