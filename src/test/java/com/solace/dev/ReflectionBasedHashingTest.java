package com.solace.dev;

import com.solacesystems.jcsmp.Topic;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionBasedHashingTest {

    @Test
    public void oneTest() {
        int maxHashCount = 1001;
        HashingStrategy<Object> strategy =
                new ReflectionBasedHashingStrategy(maxHashCount, "getId");
        Foo f = new Foo(32, "shoes", 1.234, 5678);
        String hash = strategy.makeHash(f);
        assertEquals("2300", hash);
        System.out.println("Hash[" + f.getId() + "] => " + hash);
    }
    @Test
    public void overlapTest() {
        int maxHashCount = 111;
        HashingStrategy<Object> strategy =
                new ReflectionBasedHashingStrategy(maxHashCount, "getId");

        for (int i = 0; i < maxHashCount+5; i++) {
            Foo f = new Foo(i, "shoes", 1.234, 5678);
            String hash = strategy.makeHash(f);
            assertEquals(3, hash.length());
            System.out.println("Hash[" + f.getId() + "] => " + hash);
        }
    }

    private class Thingy {
        public Thingy(long id) {
            this.id = id;
        }
        public long getID() {
            return id;
        }
        private long id;
    }

    private ReflectionBasedHashingStrategy hashingStrategy =
            new ReflectionBasedHashingStrategy(80, "getID");
    private TopicStrategy<Object> topicStrategy =
            new ReflectionBasedHashingTopicStrategy(
                    Thingy.class,
                    "{hash}/{id}",
                    hashingStrategy );

    @Test
    public void basicStrategyTopicTest() {
        Thingy t = new Thingy(5L);
        String hash = hashingStrategy.makeHash(t);
        TestCase.assertEquals("50", hash);
        Topic topic = topicStrategy.makeTopic(t);
        TestCase.assertEquals("50/5", topic.getName());
    }
}
