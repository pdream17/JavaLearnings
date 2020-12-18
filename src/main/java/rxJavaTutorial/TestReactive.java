package rxJavaTutorial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestReactive {
    private static ObjectMapper mapper = new ObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(TestReactive.class);

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new SampleModule());
        Task guiceTask = injector.getInstance(Key.get(Task.class, RandomAnnotation.class));
        List<String> someList = Arrays.asList("127.0.0.1:4800..4820");
        System.out.println(someList);
        System.out.println(guiceTask.toString());
    }


}
