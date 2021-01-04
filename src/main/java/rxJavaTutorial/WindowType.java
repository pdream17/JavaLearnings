package rxJavaTutorial;

import java.sql.Timestamp;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import rxJavaTutorial.flink.pojos.Record;

//TumblingEvent

public class WindowType
{
    public static void main(String[] args) throws Exception
    {
        // set up the streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("patternTest1", new SimpleStringSchema(), properties);
        consumer.setStartFromLatest();
        DataStream<String> data = env.addSource(consumer);

        DataStream<Tuple2<Long, String>> sum = data.map(new MapFunction<String, Tuple2<Long, String>>()
        {
            public Tuple2<Long, String> map(String s)
            {
                String[] words = s.split(",");
                return new Tuple2<Long, String>(Long.parseLong(words[0]), words[1]);
            }
        })

                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<Long, String>>()

                {
                    public long extractAscendingTimestamp(Tuple2<Long, String> t)
                    {
                        return t.f0;
                    }
                })
                .windowAll(TumblingEventTimeWindows.of(Time.seconds(5)))
                .reduce(new ReduceFunction<Tuple2<Long, String>>()
                {
                    public Tuple2<Long, String> reduce(Tuple2<Long, String> t1, Tuple2<Long, String> t2)
                    {
                        int num1 = Integer.parseInt(t1.f1);
                        int num2 = Integer.parseInt(t2.f1);
                        int sum = num1 + num2;
                        Timestamp t = new Timestamp(System.currentTimeMillis());
                        return new Tuple2<Long, String>(t.getTime(), "" + sum);
                    }
                });
        sum.print();

        // execute program
        env.execute("Window");
    }
}
