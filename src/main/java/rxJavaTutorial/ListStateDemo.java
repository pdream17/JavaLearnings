package rxJavaTutorial;

import java.sql.Timestamp;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class ListStateDemo
{
    public static void main(String[] args) throws Exception
    {
        // set up the streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> data = env.socketTextStream("localhost", 9090);

        DataStream<Tuple2<String, Long>> sum = data.map(new MapFunction<String, Tuple2<Long, String>>()
        {
            public Tuple2<Long, String> map(String s)
            {
                String[] words = s.split(",");
                return new Tuple2<Long, String>(Long.parseLong(words[0]), words[1]);
            }
        })
                .keyBy(0)
                .flatMap(new StatefulMap());
        sum.writeAsText("/home/jivesh/state2");

        // execute program
        env.execute("State");
    }

    public static class StatefulMap extends RichFlatMapFunction<Tuple2<Long, String>, Tuple2<String, Long>>
    {
        private transient ValueState<Long> count;
        private transient ListState<Long> numbers;

        public void flatMap(Tuple2<Long, String> input, Collector<Tuple2<String, Long>> out)throws Exception
        {
            Long currValue = Long.parseLong(input.f1);
            Long currCount = count.value();

            currCount += 1;

            count.update(currCount);
            numbers.add(currValue);

            if (currCount >= 10)
            {
                Long sum = 0L;
                String numbersStr = "";
                for (Long number : numbers.get())
                {
                    numbersStr = numbersStr + " " + number;
                    sum = sum + number;
                }
                /* emit sum of last 10 elements */
                out.collect(new Tuple2<String, Long>(numbersStr, sum));
                /* clear value */
                count.clear();
                numbers.clear();
            }
        }
        public void open(Configuration conf)
        {
            ListStateDescriptor<Long> listDesc = new ListStateDescriptor<Long>("numbers", Long.class);
            numbers = getRuntimeContext().getListState(listDesc);

            ValueStateDescriptor<Long> descriptor2 = new ValueStateDescriptor<Long>( "count",  Long.class, 0L);
            count = getRuntimeContext().getState(descriptor2);
        }
    }
}

