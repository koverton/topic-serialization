package com.solace.dev;

import com.solacesystems.jcsmp.Topic;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class ReflectionStrategyTest {
    private ReflectionBasedTopicStrategy strategy =
            new ReflectionBasedTopicStrategy(Foo.class, "foo/{id}/{itemName}");

    @Test
    public void basicStrategyTopicTest() {
        Foo f = new Foo(9, "shoes", 1.234, 5678);
        Topic t = strategy.makeTopic(f);
        assertEquals(t.getName(), "foo/9/shoes");
    }

    @Test
    public void nullValueTopicTest() {
        Foo f = new Foo(9, null, 1.234, 5678);
        Topic t = strategy.makeTopic(f);
        assertEquals(t.getName(), "foo/9/_");
    }

    @Test
    public void wrongObjectTypeTopicTest() {
        Topic t = strategy.makeTopic("this is a stringy thingy");
        assertEquals(t.getName(), "foo/_/_");
    }

    @Test
    public void basicStrategySubscriptionTest() {
        Map<String,String> filter = new HashMap<>();
        filter.put( "itemname", "router" );
        Topic t = strategy.makeSubscription(filter);
        assertEquals( "foo/*/router", t.getName() );
    }

    @Test
    public void emptyFilterSubscriptionTest() {
        Map<String,String> filter = new HashMap<>();
        Topic t = strategy.makeSubscription(filter);
        assertEquals( "foo/*/*", t.getName() );
    }
}
