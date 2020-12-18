package rxJavaTutorial;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FlinkKafka {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("flinkTest", new SimpleStringSchema(), properties);
        consumer.setStartFromEarliest();
        DataStream<String> stream = env.addSource(consumer);

        stream.print();
        env.execute("Flink Kafka");

    }
}

class KafkaSchema implements KafkaDeserializationSchema<Map> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isEndOfStream(Map map) {
        return false;
    }

    @Override
    public Map deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
        Map<String, String> record = new HashMap<>();
        String key = mapper.readValue(consumerRecord.key(), String.class);
        String value = mapper.readValue(consumerRecord.value(), String.class);
        record.put(key, value);
        return record;
    }

    @Override
    public TypeInformation<Map> getProducedType() {
        return TypeInformation.of(new TypeHint<Map>() {
        });
    }
}
