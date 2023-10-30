package consumer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerFilterByValue {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerFilterByValue.class);

    private static final String BOOTSTRAP_SERVERS = ":9092";
    private static final String CLIENT_ID = "ex";
    private static final String GROUP_ID = "ex3";
    private static final String OFFSET_RESET = "earliest";

    private static final String filterValue="v7";

    private static Producer<String, String> producer;


    @SuppressWarnings("boxing")
    public static void main(String[] args) {
        Properties propsProducer = new Properties();
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(propsProducer);

        Properties propsConsumer = new Properties();
        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        try (KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(propsConsumer)) {
            consumer.subscribe(Collections.singleton("events2"), new ConsumerRebalanceListener() {

                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    LOG.info("onPartitionsRevoked - partitions:{}", formatPartitions(partitions));
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    LOG.info("onPartitionsAssigned - partitions: {}", formatPartitions(partitions));

                }
            });
            while (true) {
                ConsumerRecords<String, Integer> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, Integer> data : records) {
                    LOG.info("--------------  Example Consumer ----------- topic = {}, key = {}, value = {} => partition = {}, offset= {}", args[0], data.key(), data.value(), data.partition(), data.offset());

                    if (data.value().toString().equals(filterValue)){
                        //newSingleThreadScheduledExecutor --> Creates an Executor that uses a single worker thread operating off an unbounded queue.
                       executor.scheduleAtFixedRate(() -> send("events1"), 0, 3, TimeUnit.SECONDS);

//                        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Something goes wrong: {}", e.getMessage(), e);
        }
    }
    @SuppressWarnings("boxing")
    public static String formatPartitions(Collection<TopicPartition> partitions) {
        return partitions.stream()
                .map(topicPartition -> String.format("topic: %s, partition: %s", topicPartition.topic(), topicPartition.partition()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static void send(String topic) {
        final int number = new Random().nextInt(10);
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, "key" + number, "v"+number);
        try {
            RecordMetadata meta = producer.send(data).get();
            System.out.println(String.format("----------------- Example Producer -------------- key = %s, value = %s => partition = %d, offset= %d", data.key(), data.value(), meta.partition(), meta.offset()));
            //LOG.info("----------------- Example Producer -------------- key = {}, value = {} => partition = {}, offset= {}", data.key(), data.value(), meta.partition(), meta.offset());
        } catch (InterruptedException | ExecutionException e) {
            producer.flush();
        }
    }
}