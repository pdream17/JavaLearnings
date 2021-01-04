package rxJavaTutorial.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import rxJavaTutorial.flink.pojos.Record;
import rxJavaTutorial.flink.pojos.UserActivity;

import java.io.IOException;
import java.util.Properties;

public class StreakDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flinkKafka");

        FlinkKafkaConsumer<Record> consumer = new FlinkKafkaConsumer<>("patternTest1", new RecordSchema(), properties);
        consumer.setStartFromLatest();
        DataStream<Record> stream = env.addSource(consumer);
//        stream.assignTimestampsAndWatermarks(
//                WatermarkStrategy.forBoundedOutOfOrderness(Duration.ZERO)
//        );

        //Trigger
        // OnEvent ---> Continue
        // OnEventTime/OnProcessingTime ---> seperate event (POJO -- windowStartTime, windowEndTime) produce it to kafka

        DataStream<UserActivity> finalStream =
                stream
                        .keyBy(new RecordKey())
                        .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                        .process(new StreakProcessing());

        finalStream.print();
        env.execute("Streak Demo");
    }
}

class RecordSchema implements DeserializationSchema<Record> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Record deserialize(byte[] message) throws IOException {
        return mapper.readValue(message, Record.class);
    }

    @Override
    public boolean isEndOfStream(Record nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Record> getProducedType() {
        return TypeInformation.of(new TypeHint<Record>() {});
    }
}

class RecordKey implements KeySelector<Record, Long> {

    @Override
    public Long getKey(Record value) throws Exception {
        return value.getUserId();
    }
}

class StreakProcessing extends ProcessWindowFunction<Record, UserActivity, Long, TimeWindow> {

    private MapState<Long, Boolean> mapping;

    @Override
    public void process(Long s, Context context, Iterable<Record> elements, Collector<UserActivity> out) throws Exception {
        System.out.println("Processing time is: " + context.currentProcessingTime());
        if (elements.iterator().hasNext()) {
            UserActivity activity = UserActivity.builder()
                    .userId(s)
                    .startTimestamp(context.window().getStart())
                    .endTimestamp(context.window().maxTimestamp())
                    .build();
            out.collect(activity);
        }
    }

    @Override
    public void open(Configuration parameters) {
        MapStateDescriptor<Long, Boolean> descriptor =
                new MapStateDescriptor<>("mapping",
                        Long.class,
                        Boolean.class);

        mapping = getRuntimeContext().getMapState(descriptor);
    }
}