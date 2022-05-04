package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.PulsarConsumerInterface;
import com.nnte.framework.utils.LogUtil;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PulsarClientConsumer<T> implements Runnable {
    private final static String LoggerName="PulsarClientConsumer";
    private Consumer<T> consumer;
    private boolean directAck = true;
    private PulsarConsumerInterface consumerInterface = null;
    private Executor consumerExecutor = null;
    public void startConsumer(Consumer<T> consumer,boolean directAck,
                              PulsarConsumerInterface consumerInterface,
                              int consumerThreadCount,int blockSize){
        this.consumer = consumer;
        this.directAck = directAck;
        this.consumerInterface = consumerInterface;
        new Thread(this).start();
        if (consumerThreadCount>0 && blockSize>0){
            consumerExecutor = new ThreadPoolExecutor(consumerThreadCount, consumerThreadCount, consumerThreadCount,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockSize));
        }
    }

    @Override
    public void run() {
        try {
            LogUtil.log(LoggerName, LogUtil.LogLevel.debug,
                    "consumer "+consumer.getConsumerName()+" thread start");
            while (true) {
                Message<T> message = consumer.receive();
                try {
                    LogUtil.log(LoggerName, LogUtil.LogLevel.debug,
                            "consumer "+consumer.getConsumerName()+" receive message="+message.getMessageId());
                    if (directAck)
                        consumer.acknowledge(message);
                    if (consumerInterface!=null) {
                        if (consumerExecutor==null)
                            consumerInterface.onConsumerMessageReceived(message.getValue());
                        else{
                            consumerExecutor.execute(new ConsumerProcess<T>(message.getValue(),this.consumerInterface));
                        }
                    }
                    if (!directAck)
                        consumer.acknowledge(message);
                } catch (Exception e) {
                    LogUtil.logExp(LoggerName, e);
                    consumer.negativeAcknowledge(message);
                }
            }
        }catch (PulsarClientException pce){
            LogUtil.logExp(LoggerName, pce);
        }
        LogUtil.log(LoggerName, LogUtil.LogLevel.debug,
                "consumer "+consumer.getConsumerName()+" thread closed");
    }

    static class ConsumerProcess<T> extends Thread{
        private T message=null;
        private PulsarConsumerInterface consumerInterface = null;
        public ConsumerProcess(T message,PulsarConsumerInterface consumerInterface){
            this.message = message;
            this.consumerInterface = consumerInterface;
        }
        @Override
        public void run() {
            try{
                consumerInterface.onConsumerMessageReceived(message);
            }catch (Exception e){
                LogUtil.logExp(LoggerName, e);
            }
        }
    }
}
