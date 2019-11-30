package com.solace.dev;

import static junit.framework.TestCase.assertEquals;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.Topic;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TopicFactoryTest
{
    static class FooTopicStrategy implements TopicStrategy<Foo> {
        public Topic makeTopic(Foo foo) {
            return JCSMPFactory.onlyInstance().createTopic(
                    String.format("order/%d/%s/%d",
                            foo.getId(),
                            foo.getItemName(),
                            foo.getQuantity()
                ));
        }
        private String valueOrSplat(Map<String,String> map, String field) {
            if (map.containsKey(field)) return map.get(field);
            return "*";
        }
        public Topic makeSubscription(Map<String, String> keyBasedFilters) {
            if (keyBasedFilters.size() == 0)
                return JCSMPFactory.onlyInstance().createTopic("order/>");
            return JCSMPFactory.onlyInstance().createTopic(
                    "order/" +
                            valueOrSplat(keyBasedFilters, "id")   + '/' +
                            valueOrSplat(keyBasedFilters, "item") + '/' +
                            valueOrSplat(keyBasedFilters, "quantity")
            );
        }
    }


    @Test
    public void basicPublisherTest()
    {
        TopicStrategy<Foo> strategy = new FooTopicStrategy();
        Foo foo = new Foo(333, "shoes", 1.2345, 6789);
        Topic pubTopic = strategy.makeTopic(foo);
        assertEquals("order/333/shoes/6789", pubTopic.getName());
    }

    @Test
    public void basicSubscriberTest()
    {
        TopicStrategy<Foo> strategy = new FooTopicStrategy();
        Map<String,String> filter = new HashMap<>();
        assertEquals("order/>", strategy.makeSubscription(filter).getName());

        filter.put("item", "shoes");
        assertEquals("order/*/shoes/*", strategy.makeSubscription(filter).getName());

        filter.put("id", "5555");
        assertEquals("order/5555/shoes/*", strategy.makeSubscription(filter).getName());

        filter.put("quantity", "9999");
        assertEquals("order/5555/shoes/9999", strategy.makeSubscription(filter).getName());
    }
}
