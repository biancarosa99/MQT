package producer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.*;

public class AsynchronousSimpleProducer {
    private static final Logger LOG = LoggerFactory.getLogger(AsynchronousSimpleProducer.class);

    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    // private static final String OUR_BOOTSTRAP_SERVERS = "localhost:9092, localhost:9093, localhost:9094";
    private static final String OUR_CLIENT_ID = "firstAsyncClient";

    KafkaProducer<String,String> asyncProducer;

    public AsynchronousSimpleProducer(Properties props){
        asyncProducer = new KafkaProducer<String, String>(props);
    }

    public static void main(String[] args){

        AsynchronousSimpleProducer myAsynchProducer = new AsynchronousSimpleProducer(buildProducerPropsMap());
        // send messages(records) Asynchronous

        myAsynchProducer.publishMessageAsync("events1");
        myAsynchProducer.publishMessageAsync("events2");

        try {
            sleep(5000);  // @todo comenteaza asta !!!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    Callback myCallback = (metadata, exception) -> {
        if (exception != null){
            System.out.println(String.format("Callback exception:  %s", exception.getMessage()));
            // LOG.info("Callback exception:  {}", exception.getMessage());
        }else{
            System.out.println(String.format("----------- AAAAsynchronous send ---------- topic = %s ==> partition = %d, offset = %d", metadata.topic(),  metadata.partition(), metadata.offset()));
            //  LOG.info("----------- AAAAsynchronous send ---------- topic = {} ==> partition = {}, offset = {}", metadata.topic(),  metadata.partition(), metadata.offset());
        }
    };

    public void publishMessageAsync(String topic){
        final int number = new Random().nextInt(10);
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, "key"+number, "v"+number);
        asyncProducer.send(data, myCallback);
    }

    public static Properties buildProducerPropsMap(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }
}