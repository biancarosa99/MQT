package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class SimpleExampleProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleExampleProducer.class);

    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    // private static final String OUR_BOOTSTRAP_SERVERS = "localhost:9092, localhost:9093, localhost:9094";
    private static final String OUR_CLIENT_ID = "firstProducer";

    private static Producer<String, String> producer;

    public static void main(String[] args){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,1048576);

        producer = new KafkaProducer<>(props);

        // create message for topic events2 with key
        ProducerRecord<String, String> data = new ProducerRecord<>("events2", "cheie", "event2");
        // create message for topic events2, no key
        ProducerRecord<String, String> data2 = new ProducerRecord<>("events2", "event3");

        // create message for topic events1, no key
        ProducerRecord<String, String> data1 = new ProducerRecord<>("events1",  "noKeyMessage222");
        // send messages(records) synchronous
        try {
            RecordMetadata meta = producer.send(data).get();
            LOG.info("aaaaaaaaaaaaaaaaaaaaaaaa key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), meta.partition(), meta.offset());

            RecordMetadata meta1 = producer.send(data1).get();
            LOG.info("bbbbbbbbbbbbbbbbbbbbbbbbb key = {}, value = {} ==> partition = {}, offset = {}", data1.key(), data1.value(), meta1.partition(), meta1.offset());
        }catch (InterruptedException | ExecutionException e){
            producer.flush();
        }
        producer.close();
        //call method send, with topic name
        //  send("events1");
        //  send("events2");
    }

    public static void send(String topic){
        final int number = new Random().nextInt(10);
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, "key"+number, "v"+number);
        try {
            RecordMetadata meta = producer.send(data).get();
            LOG.info("key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), meta.partition(), meta.offset());
        }catch (InterruptedException | ExecutionException e){
            producer.flush();
        }
    }

}