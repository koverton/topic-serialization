package com.solace.dev;

import com.solacesystems.jcsmp.Topic;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class ReflectionHashingTopicStrategyTest {
    private int maxHashCount = 101;
    private ReflectionBasedHashingStrategy hashingStrategy =
            new ReflectionBasedHashingStrategy(maxHashCount, "getId");
    private TopicStrategy<Object> strategy =
            new ReflectionBasedHashingTopicStrategy(Foo.class, "foo/{hash}/{id}/{itemName}", hashingStrategy);

    @Test
    public void basicStrategyTopicTest() {
        Foo f = new Foo(9, "shoes", 1.234, 5678);
        Topic t = strategy.makeTopic(f);
        assertEquals("foo/900/9/shoes", t.getName());
    }

    @Test
    public void idWrapsProperlyTopicTest() {
        Foo f = new Foo(102, "shoes", 1.234, 5678);
        Topic t = strategy.makeTopic(f);
        assertEquals("foo/100/102/shoes", t.getName());
    }

    @Test
    public void nullValueTopicTest() {
        Foo f = new Foo(90, null, 1.234, 5678);
        Topic t = strategy.makeTopic(f);
        assertEquals("foo/090/90/_", t.getName());
    }

    @Test
    public void wrongObjectTypeTopicTest() {
        Topic t = strategy.makeTopic("this is a stringy thingy");
        assertEquals("foo/_/_/_", t.getName());
    }

    @Test
    public void basicStrategySubscriptionTest() {
        Map<String,String> filter = new HashMap<>();
        filter.put( "itemname", "router" );
        Topic t = strategy.makeSubscription(filter);
        assertEquals( t.getName(), "foo/*/*/router");
    }

    @Test
    public void emptyFilterSubscriptionTest() {
        Map<String,String> filter = new HashMap<>();
        Topic t = strategy.makeSubscription(filter);
        assertEquals( t.getName(), "foo/*/*/*");
    }
}
