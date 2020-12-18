package rxJavaTutorial;


import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;

public class SampleModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Task.class).annotatedWith(RandomAnnotation.class).toInstance(getTask());
    }

    @Provides
    private Task getTask(){
        return Task.builder().combinationid(1).build();
    }
}
