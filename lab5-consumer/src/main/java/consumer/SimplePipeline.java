package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class SimplePipeline {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePipeline.class);

    //private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    private static final String OUR_BOOTSTRAP_SERVERS = "localhost:9092, localhost:9093, localhost:9094";
    private static final String OFFSET_RESET = "earliest";
    private static final String OUR_CONSUMER_GROUP_ID = "group_3";
    private static final String sourceTopicName = "events2";
    private static final String destinationTopicName = "events1";
    private static final String PRODUCER_ID = "BigEntity007";

    KafkaConsumer<String, String> kafkaConsumer;
    Producer<String, String> kafkaProducer;

    public SimplePipeline(){
        kafkaConsumer = new KafkaConsumer<String, String>(buildConsumerPropsMap());
        kafkaProducer = new KafkaProducer<String, String>(buildProducerPropsMap());
    }

    public static Properties buildConsumerPropsMap(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, OUR_CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return props;
    }
    public static Properties buildProducerPropsMap(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, PRODUCER_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    public void execute(String sourceTopic, String destinationTopic){

        kafkaConsumer.subscribe(Collections.singleton(sourceTopic));
        //kafkaConsumer.subscribe(List.of(topicName, topicName2));

        Duration pollingTime = Duration.of(2, ChronoUnit.SECONDS);
        while (true){
            // get records from kafka
            //The poll method is a blocking method waiting for specified time in seconds.
            // If no records are available after the time period specified, the poll method returns an empty ConsumerRecords.
            ConsumerRecords<String, String> records = kafkaConsumer.poll(pollingTime);

            // consume the records
            records.forEach(crtRecord -> {
                LOG.info("------ Simple Example Consumer ------------- topic ={}  key = {}, value = {} => partition = {}, offset = {}",sourceTopic, crtRecord.key(), crtRecord.value(), crtRecord.partition(), crtRecord.offset());
                if (crtRecord.value().equals("v7"))
                    kafkaProducer.send(new ProducerRecord<>(destinationTopic, crtRecord.key(), crtRecord.value().toUpperCase()));
            });
        }
    }

    public static void main(String[] args) {
        SimplePipeline pipeline = new SimplePipeline();
        pipeline.execute(sourceTopicName, destinationTopicName);
    }
}
