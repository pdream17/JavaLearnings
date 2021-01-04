package rxJavaTutorial;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.*;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PatternStreamDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.enableCheckpointing(10000);
//        env.setStateBackend(new RocksDBStateBackend("file:///Users/puneet.duggal/Desktop/Dream11/flink/states", true));

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("patternTest1", new SimpleStringSchema(), properties);
        consumer.setStartFromLatest();
        DataStream<String> stream = env.addSource(consumer);

//        DataStream<Tuple2<String, Long>> sum = stream.map((MapFunction<String, Tuple2<StrString>>) s -> {
//            String[] words = s.split(",");
//            return new Tuple2<Long, String>(Long.parseLong(words[0]), words[1]);
//        })

        //ab
        Pattern<String, ?> innerPattern =
                Pattern
                        .<String>begin("start")
                            .where(new SimpleCondition<String>() {
                                @Override
                                public boolean filter(String value) throws Exception {
                                    return value.equals("a");
                                }
                            })
                        .followedBy("end")
                            .where(new SimpleCondition<String>() {
                                @Override
                                public boolean filter(String value) throws Exception {
                                    return value.equals("b");
                                }
                            })
                        .within(Time.seconds(10));

        //ababab
        Pattern<String, ?> outerPattern =
                Pattern
                    .begin(innerPattern)
                    .times(3)
                    .consecutive();

        PatternStream<String> patternStream = CEP.pattern(stream, outerPattern);
        PatternStream<String> newPatternStream = CEP.pattern(stream, innerPattern);

        DataStream<Map> result = patternStream.process(
                new PatternProcessFunction<String, Map>() {
                    @Override
                    public void processMatch(
                            Map<String, List<String>> pattern,
                            Context ctx,
                            Collector<Map> out) throws Exception {
                        out.collect(pattern);
                    }
                });

        DataStream<Map> anotherResult = newPatternStream.process(
                new PatternProcessFunction<String, Map>() {
                    @Override
                    public void processMatch(Map<String, List<String>> match, Context ctx, Collector<Map> out) throws Exception {
                        out.collect(match);
                    }
                }
        );
        anotherResult.print();
        result.print();
        env.execute("Flink Kafka");
    }
}
