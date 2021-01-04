package rxJavaTutorial;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import java.util.Properties;

public class FlinkKafka {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStateBackend(new RocksDBStateBackend("file:///Users/puneet.duggal/Desktop/Dream11/flink/states", true));

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("kafkaFlinkTest", new SimpleStringSchema(), properties);
        consumer.setStartFromEarliest();
        DataStream<String> stream = env.addSource(consumer);

//        DataStream<Tuple2<String, Long>> sum = stream.map((MapFunction<String, Tuple2<Long, String>>) s -> {
//            String[] words = s.split(",");
//            return new Tuple2<Long, String>(Long.parseLong(words[0]), words[1]);
//        })
//                .keyBy(0)
//                .flatMap(new ListStateDemo.StatefulMap());

        stream.print();
        env.execute("Flink Kafka");

    }
}
