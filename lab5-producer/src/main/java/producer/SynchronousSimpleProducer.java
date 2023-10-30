package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class SynchronousSimpleProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SynchronousSimpleProducer.class);

    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    //  private static final String OUR_BOOTSTRAP_SERVERS = "localhost:9092, localhost:9093, localhost:9094";
    private static final String OUR_CLIENT_ID = "firstClient";

    private static Producer<String, String> producer;

    public static void main(String[] args){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        // send messages(records) synchronously
        publishMessageSynchronous("events1");
        publishMessageSynchronous("events2");
    }

    public static void publishMessageSynchronous(String topic){
        final int number = new Random().nextInt(10);
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, "key"+number, "v"+number);
        try {
            RecordMetadata meta = producer.send(data).get();
            LOG.info("----------- Synchronous send ---------- key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), meta.partition(), meta.offset());
        }catch (InterruptedException | ExecutionException e){
            producer.flush();
        }
    }

}