package rxJavaTutorial;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.Properties;

public class FlinkKafka {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<ObjectNode> consumer = new FlinkKafkaConsumer<>("flinkTest", new JSONKeyValueDeserializationSchema(true), properties);
        consumer.setStartFromEarliest();
        DataStream<ObjectNode> stream = env.addSource(consumer);

        stream.print();
        env.execute("Flink Kafka");

    }
}
