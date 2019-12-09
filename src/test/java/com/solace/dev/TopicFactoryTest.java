package com.solace.dev;

import static junit.framework.TestCase.assertEquals;

import com.solacesystems.jcsmp.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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

    @Test
    public void performanceTest() throws Exception {
        TopicStrategy<Foo> strategy = new FooTopicStrategy();
        Map<String,String> filter = new HashMap<>();
        assertEquals("order/>", strategy.makeSubscription(filter).getName());

        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, "localhost");  // msg-backbone-ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, "default"); // message-vpn
        // client-username (assumes no password)
        properties.setProperty(JCSMPProperties.USERNAME, "default");
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties,
                JCSMPFactory.onlyInstance().createContext(null),
                new SessionEventHandler() {
                    @Override
                    public void handleEvent(SessionEventArgs sessionEventArgs) {
                    }
                }
        );

        final CountDownLatch latch = new CountDownLatch(1); // used for
        // synchronizing b/w threads
        /** Anonymous inner-class for MessageListener
         *  This demonstrates the async threaded message callback */
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            public void onReceive(BytesXMLMessage msg) {
                latch.countDown();  // unblock main thread
            }
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
                latch.countDown();  // unblock main thread
            }
        });

        // Warmup
        for (Integer i = 0; i < 100; i++) {
            filter.put("quantity", i.toString());
            Topic topic = strategy.makeSubscription(filter);
            //Topic topic = JCSMPFactory.onlyInstance().createTopic(i.toString());
            session.addSubscription(topic);
            session.removeSubscription(topic);
        }

        long start = System.currentTimeMillis();
        for (Integer i = 0; i < 100000; i++) {
            filter.put("quantity", i.toString());
            Topic topic = strategy.makeSubscription(filter);
            //Topic topic = JCSMPFactory.onlyInstance().createTopic(i.toString());
            session.addSubscription(topic, (i.intValue() == 9999) ? true : false);
        }
        long end = System.currentTimeMillis();

        System.out.println("Subscribed 100k in " + (end-start) + " ms.");
        cons.start();
        // Consume-only session is now hooked up and running!

        try {
            latch.await(); // block here until message received, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }
        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();

    }
}
